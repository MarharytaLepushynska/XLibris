package com.group.xlibris.author.internal;

import com.group.xlibris.author.Author;
import com.group.xlibris.author.dto.AuthorRequest;
import com.group.xlibris.author.dto.AuthorResponse;
import com.group.xlibris.author.AuthorService;
import com.group.xlibris.author.AuthorNotFoundException;
import com.group.xlibris.author.AuthorRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;


@Service
public class AuthorServiceImpl implements AuthorService {
    private static final Logger log = LoggerFactory.getLogger(AuthorServiceImpl.class);

    private final AuthorRepository authorRepository;

    public AuthorServiceImpl(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Override
    public List<AuthorResponse> getAllAuthors() {

        log.debug("Fetching all authors");

        return authorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AuthorResponse getAuthorById(UUID id) {

        log.debug("Finding author by id={}", id);

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

        log.info("Author with id={} was created", savedAuthor.getId());

        return toResponse(savedAuthor);
    }

    @Override
    public AuthorResponse updateAuthor(UUID id, AuthorRequest request) {
        Author existingAuthor = authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));

        existingAuthor.setName(request.name());

        Author updatedAuthor = authorRepository.save(existingAuthor);

        log.info("Author information with id={} was updated", updatedAuthor.getId());

        return toResponse(updatedAuthor);
    }

    @Override
    @Transactional
    public void deleteAuthor(UUID id) {
        if (!authorRepository.existsById(id)) {
            throw new AuthorNotFoundException(id);
        }

        authorRepository.deleteById(id);

        log.info("Author with id={} was deleted", id);
    }

    @Override
    public Author getEntityById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new AuthorNotFoundException(id));
    }

    private AuthorResponse toResponse(Author author) {
        return new AuthorResponse(
                author.getId(),
                author.getName()
        );
    }
}