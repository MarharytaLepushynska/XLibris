package com.group.xlibris.genre;

import com.group.xlibris.common.NotFoundException;

import java.util.UUID;

public class GenreNotFoundException extends NotFoundException {

    public GenreNotFoundException(UUID id) {
        super("Genre with id " + id + " not found");
    }
}