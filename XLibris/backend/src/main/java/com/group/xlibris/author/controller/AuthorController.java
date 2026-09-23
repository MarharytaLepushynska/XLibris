package com.group.xlibris.author.controller;

import com.group.xlibris.author.dto.AuthorRequest;
import com.group.xlibris.author.dto.AuthorResponse;
import com.group.xlibris.author.service.AuthorService;
import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(
            @Validated(OnCreate.class)
            @RequestBody AuthorRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authorService.createAuthor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable UUID id,
            @Validated(OnUpdate.class)
            @RequestBody AuthorRequest request) {

        return ResponseEntity.ok(
                authorService.updateAuthor(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable UUID id) {

        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}