package com.group.xlibris.book.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import org.springframework.stereotype.Component;

@Component
public class BlockedToAvailableStrategy implements BookStateTransitionStrategy {

    @Override
    public boolean supports(
            BookStatus currentStatus,
            BookStatus targetStatus) {

        return currentStatus == BookStatus.BLOCKED
                && targetStatus == BookStatus.AVAILABLE;
    }

    @Override
    public void apply(Book book) {
        book.setStatus(BookStatus.AVAILABLE);
    }
}




