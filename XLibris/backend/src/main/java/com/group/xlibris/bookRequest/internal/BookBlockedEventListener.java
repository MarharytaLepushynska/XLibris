package com.group.xlibris.bookRequest.internal;

import com.group.xlibris.bookRequest.BookRequestService;
import com.group.xlibris.book.BookBlockedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookBlockedEventListener {
    private final BookRequestService bookRequestService;

    @EventListener
    public void handle(BookBlockedEvent event) {
        bookRequestService.cancelOpenRequests(event.bookId());
    }
}
