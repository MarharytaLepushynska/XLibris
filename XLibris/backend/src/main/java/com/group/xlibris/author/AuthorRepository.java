package com.group.xlibris.author;

import com.group.xlibris.author.internal.Author;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthorRepository {

    Author save(Author author);

    Optional<Author> findById(UUID id);

    List<Author> findAll();

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
