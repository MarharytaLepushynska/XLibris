package com.group.xlibris.book.internal;

import com.group.xlibris.author.Author;
import com.group.xlibris.author.AuthorService;
import com.group.xlibris.book.*;
import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.common.NotFoundException;

import com.group.xlibris.genre.Genre;
import com.group.xlibris.genre.GenreService;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.group.xlibris.book.internal.strategy.BookStateTransitionStrategy;

import java.util.List;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {
    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final List<BookStateTransitionStrategy> strategies;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final AuthorService authorService;
    private final GenreService genreService;

    public BookServiceImpl(BookRepository bookRepository,
                           List<BookStateTransitionStrategy> strategies,
                           ApplicationEventPublisher eventPublisher,
                           UserService userService,
                           AuthorService authorService,
                           GenreService genreService) {
        this.bookRepository = bookRepository;
        this.strategies = strategies;
        this.eventPublisher = eventPublisher;
        this.userService = userService;
        this.authorService = authorService;
        this.genreService = genreService;
    }

    @Override
    public List<BookResponse> getAllBooks() {

        log.debug("Fetching all books");

        return bookRepository.findAllWithAuthorAndGenre()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BookResponse getBookById(UUID id) {

        log.debug("Finding book by id={}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Book (id = " + id + ") was not found"));

        return toResponse(book);
    }

    @Override
    public Book getBookReferenceById(UUID id) {
        return bookRepository.getReferenceById(id);
    }

    @Override
    public BookResponse createBook(BookRequest request) {
        UUID id = UUID.randomUUID();
        User owner = userService.getEntityById(request.ownerId());
        Author author = request.authorId() == null
                ? null
                : authorService.getEntityById(request.authorId());

        Genre genre = request.genreId() == null
                ? null
                : genreService.getEntityById(request.genreId());

        Book book = new Book(
                id,
                request.title(),
                request.description(),
                request.photoURL(),
                BookStatus.AVAILABLE,
                owner,
                author,
                genre
        );

        Book savedBook = bookRepository.save(book);

        log.info("Book with id={} was created", savedBook.getId());

        return toResponse(savedBook);
    }

    @Override
    public BookResponse updateBook(UUID id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Book (id = " + id + ") was not found"
                        ));

        User owner = userService.getEntityById(request.ownerId());
        Author author = request.authorId() == null
                ? null
                : authorService.getEntityById(request.authorId());

        Genre genre = request.genreId() == null
                ? null
                : genreService.getEntityById(request.genreId());

        book.setTitle(request.title());
        book.setDescription(request.description());
        book.setPhotoURL(request.photoURL());
        book.setOwner(owner);
        book.setAuthor(author);
        book.setGenre(genre);

        Book updatedBook = bookRepository.save(book);

        System.out.println(
                "Book information with id " + updatedBook.getId() + " was updated"
        );

        return toResponse(updatedBook);
    }

    @Override
    public void deleteBook(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new NotFoundException(
                    "Book (id = " + id + ") was not found"
            );
        }

        bookRepository.deleteById(id);

        log.info("Book with id={} was deleted", id);
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

        log.info("Event for Book with id={} was published", id);
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

        log.info("Book with id={} was unblocked", id);
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
                                "Cannot change book status from " + book.getStatus() + " to " + targetStatus));

        BookStatus previousStatus = book.getStatus();

        strategy.apply(book);
        bookRepository.save(book);

        log.info(
                "Status of Book with id={} was changed from {} to {}",
                id,
                previousStatus,
                book.getStatus()
        );

        if (targetStatus == BookStatus.BLOCKED) {
            eventPublisher.publishEvent(new BookBlockedEvent(id));

            log.info("Event for Book with id={} was published", id);

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

    @Override
    public Book getEntityById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book (id = " + id + ") was not found"));
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getPhotoURL(),
                book.getStatus(),
                book.getOwner().getId(),
                book.getAuthor() == null ? null : book.getAuthor().getId(),
                book.getGenre() == null ? null : book.getGenre().getId()
        );
    }
}