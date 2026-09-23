package com.group.xlibris.book.service;

import com.group.xlibris.book.BookBlockedEvent;
import com.group.xlibris.book.BookRequest;
import com.group.xlibris.book.BookResponse;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.InvalidBookStateTransitionException;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.internal.strategy.BookStateTransitionStrategy;
import com.group.xlibris.bookRequest.BookRequestStatus;
import com.group.xlibris.common.NotFoundException;

import com.group.xlibris.book.internal.BookServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookStateTransitionStrategy strategy;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private BookServiceImpl bookService;

    private UUID bookId;
    private UUID ownerId;
    private UUID authorId;
    private UUID genreId;

    private Book book;
    private BookRequest request;

    @BeforeEach
    void setUp() {

        bookService = new BookServiceImpl(
                bookRepository,
                List.of(strategy),
                eventPublisher
        );

        bookId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        genreId = UUID.randomUUID();

        book = new Book(
                bookId,
                "Test Book",
                "Test description",
                "photo.jpg",
                BookStatus.AVAILABLE,
                ownerId,
                authorId,
                genreId
        );

        request = new BookRequest(
                bookId,
                "Test Book",
                "Test description",
                "photo.jpg",
                ownerId,
                authorId,
                genreId
        );
    }

    @Test
    void getBookById_shouldReturnBook_whenBookExists() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        BookResponse response = bookService.getBookById(bookId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(bookId);
        assertThat(response.title()).isEqualTo("Test Book");
        assertThat(response.description()).isEqualTo("Test description");
        assertThat(response.status()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.genreId()).isEqualTo(genreId);

        verify(bookRepository).findById(bookId);
    }

    @Test
    void getBookById_shouldThrowException_whenBookDoesNotExist() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById(bookId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book");

        verify(bookRepository).findById(bookId);
    }

    @Test
    void createBook_shouldSaveAndReturnBook() {

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.createBook(request);

        assertThat(response).isNotNull();
        assertThat(response.title()).isEqualTo("Test Book");
        assertThat(response.description()).isEqualTo("Test description");
        assertThat(response.status()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.genreId()).isEqualTo(genreId);

        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_shouldUpdateAndReturnBook_whenBookExists() {

        when(bookRepository.existsById(bookId))
                .thenReturn(true);

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.updateBook(bookId, request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(bookId);
        assertThat(response.title()).isEqualTo("Test Book");
        assertThat(response.description()).isEqualTo("Test description");
        assertThat(response.status()).isEqualTo(BookStatus.AVAILABLE);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.authorId()).isEqualTo(authorId);
        assertThat(response.genreId()).isEqualTo(genreId);

        verify(bookRepository).existsById(bookId);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_shouldThrowException_whenBookDoesNotExist() {

        when(bookRepository.existsById(bookId))
                .thenReturn(false);

        assertThatThrownBy(() -> bookService.updateBook(bookId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book");

        verify(bookRepository).existsById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_shouldDeleteBook_whenBookExists() {

        when(bookRepository.existsById(bookId))
                .thenReturn(true);

        bookService.deleteBook(bookId);

        verify(bookRepository).existsById(bookId);
        verify(bookRepository).deleteById(bookId);
    }

    @Test
    void deleteBook_shouldThrowException_whenBookDoesNotExist() {

        when(bookRepository.existsById(bookId))
                .thenReturn(false);

        assertThatThrownBy(() -> bookService.deleteBook(bookId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book");

        verify(bookRepository).existsById(bookId);
        verify(bookRepository, never()).deleteById(any());
    }


    @Test
    void blockBook_shouldBlockBook_whenBookIsAvailable() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        bookService.blockBook(bookId);

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.BLOCKED);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void unblockBook_shouldUnblockBook_whenBookIsBlocked() {

        book.setStatus(BookStatus.BLOCKED);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        bookService.unblockBook(bookId);

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.AVAILABLE);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void changeStatus_shouldThrowException_whenNoStrategySupportsTransition() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(strategy.supports(
                BookStatus.AVAILABLE,
                BookStatus.BLOCKED
        )).thenReturn(false);

        assertThatThrownBy(() ->
                bookService.changeStatus(
                        bookId,
                        BookStatus.BLOCKED
                ))
                .isInstanceOf(InvalidBookStateTransitionException.class)
                .hasMessageContaining("AVAILABLE")
                .hasMessageContaining("BLOCKED");

        verify(strategy).supports(
                BookStatus.AVAILABLE,
                BookStatus.BLOCKED
        );

        verify(strategy, never()).apply(any(Book.class));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void recalculateAndSaveBookStatus_shouldSetBorrowed_whenRequestIsFulfilled() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        bookService.recalculateAndSaveBookStatus(
                bookId,
                BookRequestStatus.FULFILLED
        );

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.BORROWED);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void shouldBlockAndPublishEvent() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        bookService.blockBook(bookId);
        assertEquals(BookStatus.BLOCKED, book.getStatus());
        verify(bookRepository).save(book);
        verify(eventPublisher).publishEvent(new BookBlockedEvent(bookId));
    }
}