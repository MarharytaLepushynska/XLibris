package com.group.xlibris.book.repository;

import com.group.xlibris.book.entity.Book;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(UUID id);

    List<Book> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);

    void deleteAll();
}