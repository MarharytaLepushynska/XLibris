package com.group.xlibris.author.service;

import com.group.xlibris.author.dto.AuthorResponse;
import com.group.xlibris.author.dto.AuthorRequest;

import java.util.List;
import java.util.UUID;

public interface AuthorService{

    List<AuthorResponse> getAllAuthors();
    AuthorResponse getAuthorById(UUID id);
    AuthorResponse createAuthor(AuthorRequest request);
    AuthorResponse updateAuthor(UUID id, AuthorRequest request);
    void deleteAuthor(UUID id);
}
