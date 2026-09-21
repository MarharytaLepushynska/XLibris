package com.group.xlibris.book.service;

import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.exception.InvalidBookStateTransitionException;
import com.group.xlibris.book.repository.BookRepository;
import com.group.xlibris.book.strategy.BookStateTransitionStrategy;
import com.group.xlibris.common.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final List<BookStateTransitionStrategy> strategies;

    public BookServiceImpl(
            BookRepository bookRepository,
            List<BookStateTransitionStrategy> strategies) {

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
        Book book = getBook(id);
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
                request.status(),
                request.ownerId(),
                request.authorId(),
                request.genreId()
        );

        return toResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse updateBook(UUID id, BookRequest request) {

        Book existingBook = getBook(id);

        Book book = new Book(
                existingBook.getId(),
                request.title(),
                request.description(),
                request.photoURL(),
                existingBook.getStatus(),
                request.ownerId(),
                request.authorId(),
                request.genreId()
        );

        return toResponse(bookRepository.save(book));
    }

    @Override
    public void deleteBook(UUID id) {

        getBook(id);

        bookRepository.deleteById(id);
    }

    @Override
    public void borrowBook(UUID id) {
        changeStatus(id, BookStatus.BORROWED);
    }

    @Override
    public void returnBook(UUID id) {
        changeStatus(id, BookStatus.AVAILABLE);
    }

    @Override
    public void blockBook(UUID id) {
        changeStatus(id, BookStatus.BLOCKED);
    }

    @Override
    public void unblockBook(UUID id) {
        changeStatus(id, BookStatus.AVAILABLE);
    }

    @Override
    public void changeStatus(UUID id, BookStatus targetStatus) {

        Book book = getBook(id);

        BookStateTransitionStrategy strategy = strategies.stream()
                .filter(s ->
                        s.supports(book.getStatus(), targetStatus))
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

    private Book getBook(UUID id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book (id = " + id + ") was not found"
                        ));
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