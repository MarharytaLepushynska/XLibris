package com.group.xlibris.genre.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Genre {

    private UUID id;
    private String name;
}