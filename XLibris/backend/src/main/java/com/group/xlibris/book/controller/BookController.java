package com.group.xlibris.book.controller;

import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.entity.Book;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final Map<UUID, Book> books = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks() {

        List<BookResponse> response = books.values()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable UUID id
    ) {

        Book book = books.get(id);

        if (book == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(toResponse(book));
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Validated(OnCreate.class) @RequestBody BookRequest request) {

        UUID id = UUID.randomUUID();

        Book book = new Book(
                id,
                request.title(),
                request.description(),
                request.photoURL(),
                request.status(),
                request.ownerId(),
                request.authorId(),
                request.genreId()
        );

        books.put(id, book);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(book));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id,
            @Validated(OnUpdate.class) @RequestBody BookRequest request) {

        if (!books.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        Book updatedBook = new Book(
                id,
                request.title(),
                request.description(),
                request.photoURL(),
                request.status(),
                request.ownerId(),
                request.authorId(),
                request.genreId()
        );

        books.put(id, updatedBook);

        return ResponseEntity.ok(toResponse(updatedBook));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable UUID id
    ) {

        if (!books.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        books.remove(id);

        return ResponseEntity.noContent().build();
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getDescription(),
                book.getPhotoURL(),
                book.getStatus(),
                book.getOwnerId(),
                book.getAuthorId(),
                book.getGenreId()
        );
    }
}
