package com.group.xlibris.genre.internal.strategy;

public interface GenreStrategy {

    boolean supports(String genreName);

    String normalize(String genreName);
}