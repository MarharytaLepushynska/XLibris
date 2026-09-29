package com.group.xlibris.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query("""
            SELECT b FROM Book b
            WHERE LOWER(b.author.name)
            LIKE LOWER(CONCAT('%', :authorName, '%'))
            """)
    List<Book> searchByAuthor(@Param("authorName") String authorName);

    @Query("""
        SELECT b FROM Book b
        LEFT JOIN FETCH b.author
        LEFT JOIN FETCH b.genre
        """)
    List<Book> findAllWithAuthorAndGenre();

    @Modifying
    @Query("UPDATE Book b SET b.author = null WHERE b.author.id = :authorId")
    void clearAuthor(@Param("authorId") UUID authorId);

    @Modifying
    @Query("UPDATE Book b SET b.genre = null WHERE b.genre.id = :genreId")
    void clearGenre(@Param("genreId") UUID genreId);
}