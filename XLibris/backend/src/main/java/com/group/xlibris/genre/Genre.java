package com.group.xlibris.genre;

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
@Table(name = "genres")
public class Genre {
    protected Genre() {}

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;
}