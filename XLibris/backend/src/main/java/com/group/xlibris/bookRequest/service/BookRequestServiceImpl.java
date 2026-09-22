package com.group.xlibris.bookRequest.service;

import com.group.xlibris.book.entity.Book;
import com.group.xlibris.book.enums.BookStatus;
import com.group.xlibris.book.repository.BookRepository;
import com.group.xlibris.bookRequest.command.CreateBookRequestCommand;
import com.group.xlibris.bookRequest.command.UpdateBookRequestCommand;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.bookRequest.entity.BookRequestEntity;
import com.group.xlibris.bookRequest.enums.BookRequestStatus;
import com.group.xlibris.bookRequest.events.BookRequestStatusChangedEvent;
import com.group.xlibris.bookRequest.exception.InvalidBookRequestStateException;
import com.group.xlibris.bookRequest.exception.InvalidBookStateException;
import com.group.xlibris.bookRequest.repository.BookRequestRepository;
import com.group.xlibris.common.exception.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookRequestServiceImpl implements BookRequestService {
    private final BookRequestRepository repository;
    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;

    public BookRequestServiceImpl(BookRequestRepository repository,
                                  BookRepository bookRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public BookRequestResponse create(CreateBookRequestCommand command) {
        Book book = bookRepository.findById(command.bookId())
                .orElseThrow(() -> new NotFoundException("Book (id= " + command.bookId() + ") was not found"));
        if (book.getOwnerId().equals(command.requesterId())) {
            throw new IllegalArgumentException("Cannot request to borrow your own book");
        }

        if (book.getStatus() != BookStatus.AVAILABLE) {
            throw new InvalidBookStateException("Book must be available to borrow");
        }

        BookRequestEntity entity = new BookRequestEntity(
                UUID.randomUUID(),
                command.bookId(),
                command.requesterId(),
                book.getOwnerId(),
                command.desiredDurationDays(),
                BookRequestStatus.PENDING,
                Instant.now(),
                null
        );
        return BookRequestResponse.from(repository.save(entity));
    }

    @Override
    public BookRequestResponse getById(UUID id) {
        return BookRequestResponse.from(findOrThrow(id));
    }

    @Override
    public List<BookRequestResponse> getAll(UUID bookId, UUID requesterId, BookRequestStatus status, int page, int size) {
        return repository.findAll().stream()
                .filter(r -> bookId == null || r.getBookId().equals(bookId))
                .filter(r -> requesterId == null || r.getRequesterId().equals(requesterId))
                .filter(r -> status == null || r.getStatus().equals(status))
                .skip((long) page*size)
                .limit(size)
                .map(BookRequestResponse::from)
                .toList();
    }

    @Override
    public BookRequestResponse updateStatus(UpdateBookRequestCommand command) {
        BookRequestEntity existing = findOrThrow(command.requestId());
        BookRequestStatus previousStatus = existing.getStatus();

        if(!previousStatus.canTransitionTo(command.targetStatus())) {
            throw new InvalidBookRequestStateException(
                    "Cannot transition BookRequest " + command.requestId() +
                    " from " + existing.getStatus() + " to " + command.targetStatus());
        }

        existing.setStatus(command.targetStatus());
        existing.setRespondedAt(Instant.now());
        BookRequestEntity saved = repository.save(existing);

        eventPublisher.publishEvent(new BookRequestStatusChangedEvent(
                saved.getId(), saved.getBookId(), saved.getRequesterId(), saved.getOwnerId(),
                saved.getDesiredDurationDays(), previousStatus, saved.getStatus()));
        return BookRequestResponse.from(saved);
    }

    @Override
    public void delete(UUID id) {
        if(!repository.existsById(id)) {
            throw new NotFoundException("Book request (id= " + id + ") was not found");
        }
        repository.deleteById(id);
    }

    private BookRequestEntity findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book request (id= " + id + ") was not found"));
    }
}
