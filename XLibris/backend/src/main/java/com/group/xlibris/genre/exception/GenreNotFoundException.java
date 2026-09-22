package com.group.xlibris.genre.exception;

import com.group.xlibris.common.exception.NotFoundException;

import java.util.UUID;

public class GenreNotFoundException extends NotFoundException {

    public GenreNotFoundException(UUID id) {
        super("Genre with id " + id + " not found");
    }
}