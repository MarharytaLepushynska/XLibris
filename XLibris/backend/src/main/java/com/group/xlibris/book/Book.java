package com.group.xlibris.book;

import com.group.xlibris.author.Author;
import com.group.xlibris.genre.Genre;
import com.group.xlibris.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book {
    protected Book() {}

    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    private String photoURL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "author_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "genre_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Genre genre;
}