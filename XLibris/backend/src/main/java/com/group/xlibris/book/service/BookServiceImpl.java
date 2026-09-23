package com.group.xlibris.book.service;

import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.exception.InvalidBookStateTransitionException;
import com.group.xlibris.book.repository.BookRepository;
import com.group.xlibris.bookRequest.enums.BookRequestStatus;
import com.group.xlibris.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import com.group.xlibris.book.strategy.BookStateTransitionStrategy;

import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final List<BookStateTransitionStrategy> strategies;

    public BookServiceImpl(BookRepository bookRepository, List<BookStateTransitionStrategy> strategies) {
        this.bookRepository = bookRepository;
        this.strategies = strategies;
    }

    @Override
    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BookResponse getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Book (id = " + id + ") was not found"));

        return toResponse(book);
    }

    @Override
    public BookResponse createBook(BookRequest request) {
        UUID id = UUID.randomUUID();

        Book book = new Book(
                id,
                request.title(),
                request.description(),
                request.photoURL(),
                BookStatus.AVAILABLE,
                request.ownerId(),
                request.authorId(),
                request.genreId()
        );

        return toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse updateBook(UUID id, BookRequest request) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundException(
                    "Book (id = " + id + ") was not found"
            );
        }

        Book book = new Book(
                id,
                request.title(),
                request.description(),
                request.photoURL(),
                BookStatus.AVAILABLE,
                request.ownerId(),
                request.authorId(),
                request.genreId()
        );

        return toResponse(bookRepository.save(book));
    }

    @Override
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundException(
                    "Book (id = " + id + ") was not found"
            );
        }

        bookRepository.deleteById(id);
    }

    private Book getBook(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book (id = " + id + ") was not found"
                        ));
    }

    @Override
    public void blockBook(UUID id) {
        Book book = getBook(id);

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new InvalidBookStateTransitionException(
                    "Book cannot be blocked from status "
                            + book.getStatus()
            );
        }

        book.setStatus(BookStatus.BLOCKED);
        bookRepository.save(book);
    }

    @Override
    public void unblockBook(UUID id) {
        Book book = getBook(id);

        if (book.getStatus() != BookStatus.BLOCKED) {
            throw new InvalidBookStateTransitionException(
                    "Book cannot be unblocked from status "
                            + book.getStatus()
            );
        }

        book.setStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);
    }

    @Override
    public void changeStatus(UUID id, BookStatus targetStatus) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book (id = " + id + ") was not found"
                        ));

        BookStateTransitionStrategy strategy = strategies.stream()
                .filter(s -> s.supports(book.getStatus(), targetStatus))
                .findFirst()
                .orElseThrow(() ->
                        new InvalidBookStateTransitionException(
                                "Cannot change book status from "
                                        + book.getStatus()
                                        + " to "
                                        + targetStatus
                        ));

        strategy.apply(book);
        bookRepository.save(book);
    }

    @Override
    public void recalculateAndSaveBookStatus(
            UUID bookId,
            BookRequestStatus requestStatus) {
        if (requestStatus != BookRequestStatus.FULFILLED) {
            return;
        }

        Book book = getBook(bookId);
        book.setStatus(BookStatus.BORROWED);
        bookRepository.save(book);
    }

    private BookStatus mapRequestStatusToBookStatus(
            BookRequestStatus requestStatus) {

        return switch (requestStatus) {
            case PENDING -> BookStatus.AVAILABLE;
            case APPROVED -> BookStatus.AVAILABLE;
            case REJECTED, CANCELLED -> BookStatus.AVAILABLE;
            case FULFILLED -> BookStatus.BORROWED;
        };
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getPhotoURL(),
                book.getStatus(),
                book.getOwnerId(),
                book.getAuthorId(),
                book.getGenreId()
        );
    }
}