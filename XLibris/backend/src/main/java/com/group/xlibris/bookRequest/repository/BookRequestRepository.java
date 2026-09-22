package com.group.xlibris.bookRequest.repository;

import com.group.xlibris.bookRequest.entity.BookRequestEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRequestRepository {
    BookRequestEntity save(BookRequestEntity bookRequest);
    Optional<BookRequestEntity> findById(UUID id);
    List<BookRequestEntity> findAll();
    boolean existsById(UUID id);
    void deleteById(UUID id);
    void deleteAll();
}
