package com.group.xlibris.author.service;

import com.group.xlibris.author.entity.Author;
import com.group.xlibris.author.AuthorRequest;
import com.group.xlibris.author.AuthorResponse;
import com.group.xlibris.author.AuthorNotFoundException;
import com.group.xlibris.author.internal.AuthorServiceImpl;
import com.group.xlibris.author.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private AuthorRepository authorRepository;

    private AuthorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthorServiceImpl(authorRepository);
    }

    @Test
    void getAllAuthors_shouldReturnAuthors() {
        Author author1 = new Author(
                UUID.randomUUID(),
                "Test Author 1"
        );

        Author author2 = new Author(
                UUID.randomUUID(),
                "Test Author 2"
        );

        when(authorRepository.findAll())
                .thenReturn(List.of(author1, author2));

        List<AuthorResponse> result = service.getAllAuthors();

        assertThat(result)
                .hasSize(2)
                .extracting(AuthorResponse::name)
                .containsExactly(
                        "Test Author 1",
                        "Test Author 2"
                );

        verify(authorRepository).findAll();
    }

    @Test
    void getAuthorById_shouldReturnAuthor() {
        UUID id = UUID.randomUUID();

        Author author = new Author(
                id,
                "Test Author"
        );

        when(authorRepository.findById(id))
                .thenReturn(Optional.of(author));

        AuthorResponse result = service.getAuthorById(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Test Author");

        verify(authorRepository).findById(id);
    }

    @Test
    void getAuthorById_shouldThrowExceptionWhenAuthorNotFound() {
        UUID id = UUID.randomUUID();

        when(authorRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAuthorById(id))
                .isInstanceOf(AuthorNotFoundException.class);

        verify(authorRepository).findById(id);
    }

    @Test
    void createAuthor_shouldSaveAndReturnAuthor() {
        AuthorRequest request = new AuthorRequest(
                null,
                "Test Author"
        );

        when(authorRepository.save(any(Author.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthorResponse result = service.createAuthor(request);

        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo("Test Author");

        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void updateAuthor_shouldUpdateAndReturnAuthor() {
        UUID id = UUID.randomUUID();

        Author existingAuthor = new Author(
                id,
                "Test Author"
        );

        AuthorRequest request = new AuthorRequest(
                id,
                "Updated Test Author"
        );

        when(authorRepository.findById(id))
                .thenReturn(Optional.of(existingAuthor));

        when(authorRepository.save(existingAuthor))
                .thenReturn(existingAuthor);

        AuthorResponse result = service.updateAuthor(id, request);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name())
                .isEqualTo("Updated Test Author");

        verify(authorRepository).findById(id);
        verify(authorRepository).save(existingAuthor);
    }

    @Test
    void updateAuthor_shouldThrowExceptionWhenAuthorNotFound() {
        UUID id = UUID.randomUUID();

        AuthorRequest request = new AuthorRequest(
                id,
                "Updated Test Author"
        );

        when(authorRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateAuthor(id, request))
                .isInstanceOf(AuthorNotFoundException.class);

        verify(authorRepository).findById(id);
        verify(authorRepository, never()).save(any(Author.class));
    }

    @Test
    void deleteAuthor_shouldDeleteAuthor() {
        UUID id = UUID.randomUUID();

        when(authorRepository.existsById(id))
                .thenReturn(true);

        service.deleteAuthor(id);

        verify(authorRepository).existsById(id);
        verify(authorRepository).deleteById(id);
    }

    @Test
    void deleteAuthor_shouldThrowExceptionWhenAuthorNotFound() {
        UUID id = UUID.randomUUID();

        when(authorRepository.existsById(id))
                .thenReturn(false);

        assertThatThrownBy(() -> service.deleteAuthor(id))
                .isInstanceOf(AuthorNotFoundException.class);

        verify(authorRepository).existsById(id);
        verify(authorRepository, never()).deleteById(any(UUID.class));
    }
}