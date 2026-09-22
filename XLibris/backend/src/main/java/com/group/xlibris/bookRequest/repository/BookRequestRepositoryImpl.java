package com.group.xlibris.bookRequest.repository;

import com.group.xlibris.bookRequest.entity.BookRequestEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BookRequestRepositoryImpl implements BookRequestRepository{

    private final Map<UUID, BookRequestEntity> requests = new ConcurrentHashMap<>();

    @Override
    public BookRequestEntity save(BookRequestEntity bookRequest) {
        requests.put(bookRequest.getId(), bookRequest);
        return bookRequest;
    }

    @Override
    public Optional<BookRequestEntity> findById(UUID id) {
        return Optional.ofNullable(requests.get(id));
    }

    @Override
    public List<BookRequestEntity> findAll() {
        return List.copyOf(requests.values());
    }

    @Override
    public boolean existsById(UUID id) {
        return requests.containsKey(id);
    }

    @Override
    public void deleteById(UUID id) {
        requests.remove(id);
    }

    @Override
    public void deleteAll() {
        requests.clear();
    }
}
