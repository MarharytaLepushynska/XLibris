package com.group.xlibris.author.exeption;

import com.group.xlibris.common.NotFoundException;

import java.util.UUID;

public class AuthorNotFoundException extends NotFoundException {

    public AuthorNotFoundException(UUID id) {
        super("Author with id " + id + " not found");
    }
}