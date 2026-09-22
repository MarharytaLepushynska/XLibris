package com.group.xlibris.author.repository;

import com.group.xlibris.author.entity.Author;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AuthorRepositoryImpl implements AuthorRepository{

    private final ConcurrentHashMap<UUID, Author> authors = new ConcurrentHashMap<>();

    @Override
    public Author save(Author author) {
        authors.put(author.getId(), author);
        return author;
    }

    @Override
    public Optional<Author> findById(UUID id) {
        return Optional.ofNullable(authors.get(id));
    }

    @Override
    public List<Author> findAll() {
        return List.copyOf(authors.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return authors.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        authors.remove(id);
    }


}
