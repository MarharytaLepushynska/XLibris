package com.group.xlibris.genre.service.strategy;

import org.springframework.stereotype.Component;

@Component
public class MultiWordGenreStrategy implements GenreStrategy {

    @Override
    public boolean supports(String genreName) {
        return genreName.trim().contains(" ");
    }

    @Override
    public String normalize(String genreName) {
        String[] words = genreName.trim().toLowerCase().split("\\s+");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }
}