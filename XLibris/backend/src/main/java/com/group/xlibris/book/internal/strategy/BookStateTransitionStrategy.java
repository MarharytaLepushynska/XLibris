package com.group.xlibris.book.internal.strategy;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.BookStatus;

public interface BookStateTransitionStrategy {

    boolean supports(BookStatus currentStatus, BookStatus targetStatus);

    void apply(Book book);
}
