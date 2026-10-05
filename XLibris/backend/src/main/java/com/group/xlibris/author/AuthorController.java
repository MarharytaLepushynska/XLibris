package com.group.xlibris.author;

import com.group.xlibris.author.dto.AuthorRequest;
import com.group.xlibris.author.dto.AuthorResponse;
import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/authors")
@Tag(name = "Book authors", description = "Endpoints for managing book authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    @Operation(summary = "Get all authors", description = "Retrieves a list of all registered book authors")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of authors successfully retrieved")
    })
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get author by ID", description = "Retrieves detailed information about a specific author by their id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Author was not found")
    })
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(authorService.getAuthorById(id));
    }

    @PostMapping
    @Operation(summary = "Create author", description = "Registers a new book author in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input data")
    })
    public ResponseEntity<AuthorResponse> createAuthor(
            @Validated(OnCreate.class)
            @RequestBody AuthorRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authorService.createAuthor(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update author", description = "Updates details of an existing author")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input data"),
            @ApiResponse(responseCode = "404", description = "Author was not found")
    })
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable UUID id,
            @Validated(OnUpdate.class)
            @RequestBody AuthorRequest request) {

        return ResponseEntity.ok(
                authorService.updateAuthor(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete author", description = "Removes an author record from the system by id")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Author successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Author was not found")
    })
    public ResponseEntity<Void> deleteAuthor(
            @PathVariable UUID id) {

        authorService.deleteAuthor(id);
        return ResponseEntity.noContent().build();
    }
}