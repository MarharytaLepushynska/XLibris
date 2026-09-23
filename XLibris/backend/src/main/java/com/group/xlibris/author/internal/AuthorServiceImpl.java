package com.group.xlibris.author.internal;

import com.group.xlibris.AuthorRequest;
import com.group.xlibris.author.AuthorResponse;
import com.group.xlibris.author.AuthorService;
import com.group.xlibris.author.entity.Author;
import com.group.xlibris.author.AuthorNotFoundException;
import com.group.xlibris.author.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public List<AuthorResponse> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AuthorResponse getAuthorById(UUID id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        return toResponse(author);
    }

    @Override
    public AuthorResponse createAuthor(AuthorRequest request) {
        UUID id = UUID.randomUUID();

        Author author = new Author(
                id,
                request.name()
        );

        Author savedAuthor = authorRepository.save(author);

        return toResponse(savedAuthor);
    }

    @Override
    public AuthorResponse updateAuthor(UUID id, AuthorRequest request) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        existingAuthor.setName(request.name());

        Author updatedAuthor = authorRepository.save(existingAuthor);

        return toResponse(updatedAuthor);
    }

    @Override
    public void deleteAuthor(UUID id) {
        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFoundException(id);
        }

        authorRepository.deleteById(id);
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getName()
        );
    }
}