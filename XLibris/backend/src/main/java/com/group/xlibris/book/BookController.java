package com.group.xlibris.book;

import com.group.xlibris.book.dto.BookRequest;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;

import com.group.xlibris.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "Books", description = "Endpoints for managing books and tracking their availability status")
public class BookController {

    private final BookService bookService;
    private final UserService userService;

    public BookController(BookService bookService, UserService userService) {
        this.bookService = bookService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves a list of all books")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of books successfully retrieved")
    })
    public ResponseEntity<List<BookResponse>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves detailed information about a specific book by its id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<BookResponse> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping
    @Operation(summary = "Create book", description = "Registers a new book with initiial AVAILABLE status")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input data")
    })
    public ResponseEntity<BookResponse> createBook(
            @Validated(OnCreate.class) @RequestBody BookRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookService.createBook(request));
    }


    @PutMapping("/{id}")
    @Operation(summary = "Update book",
            description = "Updates details of a book owned by the current user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Book successfully updated"),
            @ApiResponse(responseCode = "403",
                    description = "User is not the book owner"),
            @ApiResponse(responseCode = "404",
                    description = "Book not found")
    })
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable UUID id,
            @Validated(OnUpdate.class) @RequestBody BookRequest request,
            Authentication authentication) {

        UUID currentUserId = userService.getUserByEmail(authentication.getName());

        return ResponseEntity.ok(bookService.updateBook(id, request, currentUserId));
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Removes a book from the system by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}