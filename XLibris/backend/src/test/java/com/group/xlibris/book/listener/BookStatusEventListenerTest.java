package com.group.xlibris.book.listener;

import com.group.xlibris.book.service.BookService;
import com.group.xlibris.bookRequest.BookRequestStatus;
import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookStatusEventListenerTest {

    @Mock
    private BookService bookService;

    @Test
    void shouldRecalculateBookStatus_whenBookRequestStatusChanged() {

        UUID bookId = UUID.randomUUID();

        BookRequestStatusChangedEvent event =
                new BookRequestStatusChangedEvent(
                        UUID.randomUUID(),
                        bookId,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        14,
                        BookRequestStatus.PENDING,
                        BookRequestStatus.APPROVED
                );

        BookStatusEventListener listener =
                new BookStatusEventListener(bookService);

        listener.handleBookRequestStatusChanged(event);

        verify(bookService).recalculateAndSaveBookStatus(
                bookId,
                BookRequestStatus.APPROVED
        );
    }
}