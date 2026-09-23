package com.group.xlibris.book.internal;

import com.group.xlibris.book.BookRequest;
import com.group.xlibris.book.BookResponse;
import com.group.xlibris.book.BookService;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.InvalidBookStateTransitionException;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.BookBlockedEvent;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.common.NotFoundException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.group.xlibris.book.internal.strategy.BookStateTransitionStrategy;

import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final List<BookStateTransitionStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;

    public BookServiceImpl(BookRepository bookRepository,
                           List<BookStateTransitionStrategy> strategies,
                           ApplicationEventPublisher eventPublisher) {
        this.bookRepository = bookRepository;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
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
        eventPublisher.publishEvent(new BookBlockedEvent(id));
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

        if (targetStatus == BookStatus.BLOCKED) {
            eventPublisher.publishEvent(new BookBlockedEvent(id));
        }
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

    @Override
    public void markAvailableAfterReturn(UUID bookId) {
        Book book = getBook(bookId);
        if (book.getStatus() != BookStatus.BORROWED) {
            return;
        }
        book.setStatus(BookStatus.AVAILABLE);
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