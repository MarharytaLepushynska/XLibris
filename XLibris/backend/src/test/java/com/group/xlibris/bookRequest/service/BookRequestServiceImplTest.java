package com.group.xlibris.bookRequest.service;

import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.service.BookService;
import com.group.xlibris.bookRequest.internal.*;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.bookRequest.BookRequestStatus;
import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import com.group.xlibris.bookRequest.DuplicateBookRequestException;
import com.group.xlibris.bookRequest.InvalidBookRequestStateException;
import com.group.xlibris.bookRequest.InvalidBookStateException;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.common.AccessDeniedException;
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

    private BookRequestServiceImpl service;
    private UUID ownerId;
    private UUID borrowerId;
    private Book book;
    private BookRequestEntity request;

    @BeforeEach
    void setUp() {
        service = new BookRequestServiceImpl(repository, bookService, eventPublisher);
        ownerId = UUID.randomUUID();
        borrowerId = UUID.randomUUID();
        book = new Book(UUID.randomUUID(), "Book", "Description", null,
                BookStatus.AVAILABLE, ownerId, UUID.randomUUID(), UUID.randomUUID());

        request = new BookRequestEntity(UUID.randomUUID(), book.getId(), borrowerId,
                ownerId, 14, BookRequestStatus.PENDING, Instant.now(), null);
    }

    @Test
    void shouldCreateRequestForAvailableBook() {
        book.setStatus(BookStatus.AVAILABLE);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
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
    void shouldCreateRequestForBorrowedBook() {
        book.setStatus(BookStatus.BORROWED);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
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

        assertThrows(DuplicateBookRequestException.class, () -> service.create(
                new CreateBookRequestCommand(book.getId(), borrowerId, 14)));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateDuplicateApprovedRequest() {
        request.setStatus(BookRequestStatus.APPROVED);
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
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
        BookRequestStatus previous = BookRequestStatus.PENDING;
        UUID actor = ownerId;
        request.setStatus(previous);

        when(repository.findById(request.getId())).thenReturn(Optional.of(request));
        when(bookService.getBookById(book.getId())).thenReturn(new BookResponse(book.getId(),
                "Kapitoshka", "some", null, BookStatus.AVAILABLE,
                ownerId, UUID.randomUUID(), UUID.randomUUID()));
        when(repository.findAll()).thenReturn(List.of(request));
        when(repository.save(any(BookRequestEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequestResponse result = service.updateStatus(new UpdateBookRequestCommand(request.getId(), actor, BookRequestStatus.APPROVED));
        assertEquals(BookRequestStatus.APPROVED, result.status());
        assertNotNull(result.respondedAt());
        verify(repository).save(request);
        verify(eventPublisher).publishEvent(new BookRequestStatusChangedEvent(request.getId(), book.getId(),
                borrowerId, ownerId, 14, previous, BookRequestStatus.APPROVED));
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