package com.group.xlibris.book.repository;

import com.group.xlibris.book.entity.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final ConcurrentHashMap<UUID, Book> books = new ConcurrentHashMap<>();

    @Override
    public Book save(Book book) {
        books.put(book.getId(), book);
        return book;
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(books.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return books.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        books.remove(id);
    }

    @Override
    public void deleteAll() {
        books.clear();
    }

}