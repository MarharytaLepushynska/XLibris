package com.group.xlibris.author.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Author {

    private UUID id;
    private String name;
}