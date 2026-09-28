package com.group.xlibris.bookRequest.service;

import com.group.xlibris.author.Author;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.Book;

import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.BookService;
import com.group.xlibris.bookRequest.internal.*;

import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import com.group.xlibris.bookRequest.DuplicateBookRequestException;
import com.group.xlibris.bookRequest.InvalidBookRequestStateException;
import com.group.xlibris.bookRequest.InvalidBookStateException;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.common.AccessDeniedException;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.user.Role;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookRequestServiceImplTest {
    @Mock
    private BookRequestRepository repository;

    @Mock
    private BookService bookService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private UserService userService;

    private BookRequestServiceImpl service;
    private UUID ownerId;
    private UUID borrowerId;
    private Book book;
    private BookRequestEntity request;
    private UUID authorId;
    private UUID genreId;

    private User user;
    private User user2;
    private Author author;
    private Genre genre;

    @BeforeEach
    void setUp() {
        service = new BookRequestServiceImpl(repository, bookService, eventPublisher, userService);
        ownerId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        authorId = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");
        genreId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        user = new User(ownerId, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);
        user2 = new User(borrowerId, "Max", "Lviv", null, "max@gmail.com",
                "+380998876447", Instant.now(), Role.USER,
                4.0, 2.9, 5, 6, 0);

        author = new Author(authorId, "JK Rowling");
        genre = new Genre(genreId, "Horror");
        book = new Book(UUID.randomUUID(), "Book", "Description", null,
                BookStatus.AVAILABLE, user, author, genre);
        request = new BookRequestEntity(UUID.randomUUID(), book, user2,
                user, 14, BookRequestStatus.PENDING, Instant.now(), null);
    }

    @Test
    void shouldCreateRequestForAvailableBook() {
        book.setStatus(BookStatus.AVAILABLE);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(BookRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getEntityById(borrowerId)).thenReturn(user2);
        when(bookService.getEntityById(book.getId())).thenReturn(book);

        BookRequestResponse result = service.create(new CreateBookRequestCommand(book.getId(), borrowerId, 14));
        assertNotNull(result.id());
        assertEquals(BookRequestStatus.PENDING, result.status());
        assertEquals(ownerId, result.ownerId());
        assertEquals(borrowerId, result.requesterId());
        verify(repository).save(any(BookRequestEntity.class));
    }

    @Test
    void shouldCreateRequestForBorrowedBook() {
        book.setStatus(BookStatus.BORROWED);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                book.getTitle(), book.getDescription(), book.getPhotoURL(), BookStatus.BORROWED,
                ownerId, authorId, genreId));
        when(userService.getEntityById(borrowerId)).thenReturn(user2);
        when(bookService.getEntityById(book.getId())).thenReturn(book);
        when(repository.findAll()).thenReturn(List.of());
        when(repository.save(any(BookRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequestResponse result = service.create(new CreateBookRequestCommand(book.getId(), borrowerId, 14));
        assertNotNull(result.id());
        assertEquals(BookRequestStatus.PENDING, result.status());
        assertEquals(ownerId, result.ownerId());
        assertEquals(borrowerId, result.requesterId());
        verify(repository).save(any(BookRequestEntity.class));
    }

    @Test
    void shouldThrowDuplicateDuplicatePendingRequest() {
        request.setStatus(BookRequestStatus.PENDING);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findAll()).thenReturn(List.of(request));
        when(userService.getEntityById(borrowerId)).thenReturn(user2);
        when(bookService.getEntityById(book.getId())).thenReturn(book);

        assertThrows(DuplicateBookRequestException.class, () -> service.create(
                new CreateBookRequestCommand(book.getId(), borrowerId, 14)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateDuplicateApprovedRequest() {
        request.setStatus(BookRequestStatus.APPROVED);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                book.getTitle(), book.getDescription(), book.getPhotoURL(), BookStatus.AVAILABLE,
                ownerId, authorId, genreId));

        when(userService.getEntityById(borrowerId)).thenReturn(user2);
        when(bookService.getEntityById(book.getId())).thenReturn(book);
        when(repository.findAll()).thenReturn(List.of(request));

        assertThrows(DuplicateBookRequestException.class, () -> service.create(
                new CreateBookRequestCommand(book.getId(), borrowerId, 14)));

        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldCreateRequestAfterCancellation() {
        request.setStatus(BookRequestStatus.CANCELLED);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findAll()).thenReturn(List.of(request));
        when(repository.save(any(BookRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userService.getEntityById(borrowerId)).thenReturn(user2);
        when(bookService.getEntityById(book.getId())).thenReturn(book);

        BookRequestResponse result = service.create(new CreateBookRequestCommand(book.getId(), borrowerId, 14));
        assertEquals(BookRequestStatus.PENDING, result.status());
        assertNotEquals(request.getId(), result.id());
    }

    @Test
    void shouldRejectBlockedBook() {
        book.setStatus(BookStatus.BLOCKED);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, book.getStatus(),
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        assertThrows(InvalidBookStateException.class, () -> service.create(
                new CreateBookRequestCommand(book.getId(), borrowerId, 14)
        ));
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldRejectOwnBook() {
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        assertThrows(IllegalArgumentException.class, () -> service.create(
                new CreateBookRequestCommand(book.getId(), ownerId, 14)
        ));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectMissingBook() {
        when(bookService.getBookById(book.getId())).thenThrow(new NotFoundException("Book not found"));
        assertThrows(NotFoundException.class, () -> service.create(
                new CreateBookRequestCommand(book.getId(), ownerId, 14)

        ));
        verify(repository, never()).save(any());
    }

    @Test
    void shouldSaveApprovedStatusAndPublishEvent() {
        request.setStatus(BookRequestStatus.PENDING);

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                book.getTitle(), book.getDescription(), book.getPhotoURL(), BookStatus.AVAILABLE,
                ownerId, authorId, genreId));

        when(repository.findAll()).thenReturn(List.of(request));
        when(repository.save(any(BookRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequestResponse result = service.updateStatus(new UpdateBookRequestCommand(request.getId(), ownerId, BookRequestStatus.APPROVED));
        assertEquals(BookRequestStatus.APPROVED, result.status());
        assertNotNull(result.respondedAt());
        verify(repository).save(request);
        verify(eventPublisher).publishEvent(new BookRequestStatusChangedEvent(request.getId(), book.getId(),
                borrowerId, ownerId, 14, BookRequestStatus.PENDING, BookRequestStatus.APPROVED));
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    void shouldSaveFulfilledStatusAndPublishEvent() {
        BookRequestStatus previous = BookRequestStatus.APPROVED;
        UUID actor = borrowerId;
        request.setStatus(previous);

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findAll()).thenReturn(List.of(request));
        when(repository.save(any(BookRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequestResponse result = service.updateStatus(new UpdateBookRequestCommand(request.getId(), actor, BookRequestStatus.FULFILLED));
        assertEquals(BookRequestStatus.FULFILLED, result.status());
        assertNotNull(result.respondedAt());
        verify(repository).save(request);
        verify(eventPublisher).publishEvent(new BookRequestStatusChangedEvent(request.getId(), book.getId(),
                borrowerId, ownerId, 14, previous, BookRequestStatus.FULFILLED));
        assertEquals(BookStatus.AVAILABLE, book.getStatus());
    }

    @Test
    void shouldRejectFulfilledBeforeApproved() {
        request.setStatus(BookRequestStatus.FULFILLED);
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        assertThrows(InvalidBookRequestStateException.class, () -> service.updateStatus(
                new UpdateBookRequestCommand(request.getId(), borrowerId, BookRequestStatus.FULFILLED)
        ));
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @EnumSource(value = BookRequestStatus.class, names = {"APPROVED", "FULFILLED", "REJECTED", "CANCELLED"})
    void shouldRejectUnrelatedActor(BookRequestStatus target) {
        if (target == BookRequestStatus.FULFILLED) {
            request.setStatus(BookRequestStatus.APPROVED);
        }
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));

        assertThrows(AccessDeniedException.class, () -> service.updateStatus(
                new UpdateBookRequestCommand(request.getId(), UUID.randomUUID(), target)
        ));
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @ParameterizedTest
    @EnumSource(value = BookStatus.class, names = {"BORROWED", "BLOCKED"})
    void shouldRejectApprovalOfUnavailableBook(BookStatus status) {
        book.setStatus(status);
        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, book.getStatus(),
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        assertThrows(InvalidBookStateException.class, () -> service.updateStatus(
                new UpdateBookRequestCommand(request.getId(), ownerId, BookRequestStatus.APPROVED)
        ));
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}