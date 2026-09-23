package com.group.xlibris.bookRequest.listener;

import com.group.xlibris.bookRequest.service.BookRequestService;
import com.group.xlibris.landCommon.BookBlockedEvent;
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
