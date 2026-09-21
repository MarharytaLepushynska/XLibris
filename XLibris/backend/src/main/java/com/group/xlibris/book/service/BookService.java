package com.group.xlibris.book.service;

import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.enums.BookStatus;

import java.util.List;
import java.util.UUID;

public interface BookService {

    List<BookResponse> getAllBooks();

    BookResponse getBookById(UUID id);

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(UUID id, BookRequest request);

    void deleteBook(UUID id);

    void borrowBook(UUID id);

    void returnBook(UUID id);

    void blockBook(UUID id);

    void unblockBook(UUID id);

    void changeStatus(UUID id, BookStatus targetStatus);
}