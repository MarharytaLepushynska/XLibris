package com.group.xlibris.book.internal.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.BookStatus;
import org.springframework.stereotype.Component;

@Component
public class AvailableToBlockedStrategy implements BookStateTransitionStrategy {

    @Override
    public boolean supports(
            BookStatus currentStatus,
            BookStatus targetStatus) {

        return currentStatus == BookStatus.AVAILABLE
                && targetStatus == BookStatus.BLOCKED;
    }

    @Override
    public void apply(Book book) {
        book.setStatus(BookStatus.BLOCKED);
    }
}