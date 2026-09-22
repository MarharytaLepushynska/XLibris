package com.group.xlibris.genre.service.strategy;

import org.springframework.stereotype.Component;

@Component
public class SingleWordGenreStrategy implements GenreStrategy {

    @Override
    public boolean supports(String genreName) {
        return !genreName.trim().contains(" ");
    }

    @Override
    public String normalize(String genreName) {
        String name = genreName.trim();

        return name.substring(0, 1).toUpperCase()
                + name.substring(1).toLowerCase();
    }
}