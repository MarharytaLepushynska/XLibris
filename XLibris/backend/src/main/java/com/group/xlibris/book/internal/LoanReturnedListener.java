package com.group.xlibris.book.internal;

import com.group.xlibris.book.BookService;
import com.group.xlibris.loan.LoanReturnedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoanReturnedListener {
    private final BookService bookService;

    @EventListener
    public void handle(LoanReturnedEvent event) {
        bookService.markAvailableAfterReturn(event.bookId());
    }
}
