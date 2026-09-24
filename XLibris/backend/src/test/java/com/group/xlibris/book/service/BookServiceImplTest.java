package com.group.xlibris.book.service;

import com.group.xlibris.book.BookBlockedEvent;
import com.group.xlibris.book.BookRequest;
import com.group.xlibris.book.BookResponse;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.InvalidBookStateTransitionException;
import com.group.xlibris.book.BookRepository;
import com.group.xlibris.book.internal.strategy.BookStateTransitionStrategy;
import com.group.xlibris.common.BookRequestStatus;
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
    void getAllBooks_shouldReturnAllBooks() {

        Book secondBook = new Book(
                UUID.randomUUID(),
                "Second Book",
                "Second description",
                "second.jpg",
                BookStatus.BLOCKED,
                ownerId,
                authorId,
                genreId
        );

        when(bookRepository.findAll())
                .thenReturn(List.of(book, secondBook));

        List<BookResponse> response = bookService.getAllBooks();

        assertThat(response)
                .hasSize(2);

        assertThat(response.get(0).id())
                .isEqualTo(book.getId());

        assertThat(response.get(0).title())
                .isEqualTo("Test Book");

        assertThat(response.get(0).status())
                .isEqualTo(BookStatus.AVAILABLE);

        assertThat(response.get(1).id())
                .isEqualTo(secondBook.getId());

        assertThat(response.get(1).title())
                .isEqualTo("Second Book");

        assertThat(response.get(1).status())
                .isEqualTo(BookStatus.BLOCKED);

        verify(bookRepository).findAll();
    }

    @Test
    void updateBook_shouldUpdateAndReturnBook_whenBookExists() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

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

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_shouldPreserveStatus_whenBookIsBorrowed() {

        book.setStatus(BookStatus.BORROWED);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.updateBook(bookId, request);

        assertThat(response.status())
                .isEqualTo(BookStatus.BORROWED);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_shouldPreserveStatus_whenBookIsBlocked() {

        book.setStatus(BookStatus.BLOCKED);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.updateBook(bookId, request);

        assertThat(response.status())
                .isEqualTo(BookStatus.BLOCKED);

        verify(bookRepository).findById(bookId);
        verify(bookRepository).save(book);
    }

    @Test
    void updateBook_shouldThrowException_whenBookDoesNotExist() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                bookService.updateBook(bookId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Book");

        verify(bookRepository).findById(bookId);
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
    void changeStatus_shouldChangeStatus_whenStrategySupportsTransition() {

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(strategy.supports(
                BookStatus.AVAILABLE,
                BookStatus.BLOCKED
        )).thenReturn(true);

        doAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setStatus(BookStatus.BLOCKED);
            return null;
        }).when(strategy).apply(book);

        bookService.changeStatus(
                bookId,
                BookStatus.BLOCKED
        );

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.BLOCKED);

        verify(strategy).supports(
                BookStatus.AVAILABLE,
                BookStatus.BLOCKED
        );

        verify(strategy).apply(book);
        verify(bookRepository).save(book);
        verify(eventPublisher).publishEvent(
                new BookBlockedEvent(bookId)
        );
    }

    @Test
    void changeStatus_shouldThrowException_whenBorrowedBookIsBlocked() {

        book.setStatus(BookStatus.BORROWED);

        when(bookRepository.findById(bookId))
                .thenReturn(Optional.of(book));

        when(strategy.supports(
                BookStatus.BORROWED,
                BookStatus.BLOCKED
        )).thenReturn(false);

        assertThatThrownBy(() ->
                bookService.changeStatus(
                        bookId,
                        BookStatus.BLOCKED
                ))
                .isInstanceOf(InvalidBookStateTransitionException.class)
                .hasMessageContaining("BORROWED")
                .hasMessageContaining("BLOCKED");

        verify(strategy).supports(
                BookStatus.BORROWED,
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
    void recalculateAndSaveBookStatus_shouldDoNothing_whenRequestIsNotFulfilled() {

        book.setStatus(BookStatus.AVAILABLE);

        bookService.recalculateAndSaveBookStatus(
                bookId,
                BookRequestStatus.APPROVED
        );

        assertThat(book.getStatus())
                .isEqualTo(BookStatus.AVAILABLE);

        verify(bookRepository, never()).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
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