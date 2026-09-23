package com.group.xlibris.bookRequest.internal;

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
