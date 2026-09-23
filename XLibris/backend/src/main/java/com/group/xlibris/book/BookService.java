package com.group.xlibris.book;

import com.group.xlibris.book.BookRequest;
import com.group.xlibris.book.BookResponse;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.bookRequest.BookRequestStatus;
import org.springframework.modulith.NamedInterface;

import java.util.List;
import java.util.UUID;

public interface BookService {

    List<BookResponse> getAllBooks();

    BookResponse getBookById(UUID id);

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(UUID id, BookRequest request);

    void deleteBook(UUID id);

    void blockBook(UUID id);

    void unblockBook(UUID id);

    void changeStatus(UUID id, BookStatus targetStatus);

    void recalculateAndSaveBookStatus(UUID bookId, BookRequestStatus requestStatus);

    void markAvailableAfterReturn(UUID bookId);

}