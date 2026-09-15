package com.group.xlibris.author.controller;

import com.group.xlibris.author.dto.AuthorRequest;
import com.group.xlibris.author.dto.AuthorResponse;
import com.group.xlibris.author.entity.Author;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final Map<UUID, Author> authors = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {

        List<AuthorResponse> response = authors.values()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable("id") UUID id
    ) {

        Author author = authors.get(id);

        if (author == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(author));
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(
            @Validated(OnCreate.class) @RequestBody AuthorRequest request
    ) {

        UUID id = UUID.randomUUID();

        Author author = new Author(
                id,
                request.name()
        );

        authors.put(id, author);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(author));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable("id") UUID id,
            @Validated(OnUpdate.class) @RequestBody AuthorRequest request
    ) {

        if (!authors.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        Author updatedAuthor = new Author(
                id,
                request.name()
        );

        authors.put(id, updatedAuthor);

        return ResponseEntity.ok(toResponse(updatedAuthor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable("id") UUID id
    ) {

        if (!authors.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        authors.remove(id);

        return ResponseEntity.noContent().build();
    }

    private AuthorResponse toResponse(Author author) {

        return new AuthorResponse(
                author.getId(),
                author.getName()
        );
    }
}