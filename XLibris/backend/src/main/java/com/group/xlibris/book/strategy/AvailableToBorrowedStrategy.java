package com.group.xlibris.book.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import org.springframework.stereotype.Component;

@Component
public class AvailableToBorrowedStrategy
        implements BookStateTransitionStrategy {

    @Override
    public boolean supports(
            BookStatus currentStatus,
            BookStatus targetStatus) {

        return currentStatus == BookStatus.AVAILABLE
                && targetStatus == BookStatus.BORROWED;
    }

    @Override
    public void apply(Book book) {
        book.setStatus(BookStatus.BORROWED);
    }
}
