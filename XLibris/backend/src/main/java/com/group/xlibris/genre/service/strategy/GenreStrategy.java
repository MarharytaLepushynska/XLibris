package com.group.xlibris.genre.service.strategy;

public interface GenreStrategy {

    boolean supports(String genreName);

    String normalize(String genreName);
}