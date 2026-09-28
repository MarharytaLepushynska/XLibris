package com.group.xlibris.author;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Entity
@Table(name = "authors")
public class Author {
    protected Author() {}

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;
}