package com.group.xlibris.book.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;

public interface BookStateTransitionStrategy {

    boolean supports(BookStatus currentStatus, BookStatus targetStatus);

    void apply(Book book);
}
