package com.group.xlibris.bookRequest.internal;

import com.group.xlibris.book.BookService;
import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookStatusEventListener {

    private final BookService bookService;

    @EventListener
    public void handleBookRequestStatusChanged(BookRequestStatusChangedEvent event) {
        bookService.recalculateAndSaveBookStatus(event.bookId(), event.newStatus());
    }
}