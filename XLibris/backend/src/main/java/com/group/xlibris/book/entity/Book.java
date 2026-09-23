package com.group.xlibris.book.entity;

import com.group.xlibris.book.BookStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class Book {

    private UUID id;
    private String title;
    private String description;
    private String photoURL;
    private BookStatus status;
    private UUID ownerId;
    private UUID authorId;
    private UUID genreId;
}