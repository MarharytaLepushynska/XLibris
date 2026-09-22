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
import com.group.xlibris.bookRequest.exception.DuplicateBookRequestException;
import com.group.xlibris.bookRequest.exception.InvalidBookRequestStateException;
import com.group.xlibris.bookRequest.exception.InvalidBookStateException;
import com.group.xlibris.bookRequest.repository.BookRequestRepository;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.command.CreateLoanCommand;
import com.group.xlibris.loan.service.LoanService;
import com.group.xlibris.user.exception.AccessDeniedException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class BookRequestServiceImpl implements BookRequestService {
    private final BookRequestRepository repository;
    private final BookRepository bookRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final LoanService loanService;

    public BookRequestServiceImpl(BookRequestRepository repository,
                                  BookRepository bookRepository,
                                  ApplicationEventPublisher eventPublisher,
                                  LoanService loanService) {
        this.repository = repository;
        this.bookRepository = bookRepository;
        this.eventPublisher = eventPublisher;
        this.loanService = loanService;
    }

    @Override
    public BookRequestResponse create(CreateBookRequestCommand command) {
        Book book = bookRepository.findById(command.bookId())
                .orElseThrow(() -> new NotFoundException("Book (id= " + command.bookId() + ") was not found"));
        if (book.getOwnerId().equals(command.requesterId())) {
            throw new IllegalArgumentException("Cannot request to borrow your own book");
        }

        if (book.getStatus() == BookStatus.BLOCKED) {
            throw new InvalidBookStateException("Book is blocked");
        }

        boolean existsAlready = getOpenRequests(command.bookId()).stream()
                .anyMatch(r -> r.getRequesterId().equals(command.requesterId()));
        if (existsAlready) {
            throw new DuplicateBookRequestException("Request for this book was already created");
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
        BookRequestStatus target = command.targetStatus();

        if(!previousStatus.canTransitionTo(target)) {
            throw new InvalidBookRequestStateException(
                    "Cannot transition BookRequest " + command.requestId() +
                    " from " + existing.getStatus() + " to " + command.targetStatus());
        }

        Book book = bookRepository.findById(existing.getBookId())
                .orElseThrow(() -> new NotFoundException("Book was not found"));
        checkActor(existing, book, command.actorId(), target);

        if (target == BookRequestStatus.APPROVED || target == BookRequestStatus.FULFILLED) {
            if (book.getStatus() != BookStatus.AVAILABLE) {
                throw new InvalidBookStateException("Book must be available");
            }
            checkFirstInQueue(existing);
        }

        return saveStatus(existing, target);
    }

    @Override
    public void delete(UUID id) {
        if(!repository.existsById(id)) {
            throw new NotFoundException("Book request (id= " + id + ") was not found");
        }
        repository.deleteById(id);
    }

    @Override
    public void cancelOpenRequests(UUID bookId) {
        for (BookRequestEntity request : getOpenRequests(bookId)) {
            saveStatus(request, BookRequestStatus.CANCELLED);
        }
    }


    private BookRequestEntity findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book request (id= " + id + ") was not found"));
    }

    private List<BookRequestEntity> getOpenRequests(UUID bookId) {
        return repository.findAll().stream()
                .filter(r -> r.getBookId().equals(bookId))
                .filter(r -> r.getStatus().isOpen())
                .sorted(Comparator.comparing(BookRequestEntity::getCreatedAt)
                        .thenComparing(BookRequestEntity::getId))
                .toList();
    }

    private void checkFirstInQueue(BookRequestEntity request) {
        List<BookRequestEntity> queue = getOpenRequests(request.getBookId());
        if (queue.isEmpty() || !queue.getFirst().getId().equals(request.getId())) {
            throw new InvalidBookRequestStateException("Only the first request can be confirmed");
        }
    }

    private void checkActor(BookRequestEntity request, Book book,
                            UUID actorId, BookRequestStatus target) {
        boolean allowed = switch (target) {
            case APPROVED, REJECTED -> book.getOwnerId().equals(actorId);
            case FULFILLED, CANCELLED -> request.getRequesterId().equals(actorId);
            default -> false;
        };
        if (!allowed) {
            throw new AccessDeniedException("You cannot perform this action");
        }
    }

    private BookRequestResponse saveStatus(BookRequestEntity request, BookRequestStatus target) {
        BookRequestStatus previous = request.getStatus();
        request.setStatus(target);
        request.setRespondedAt(Instant.now());
        BookRequestEntity saved = repository.save(request);

        eventPublisher.publishEvent(new BookRequestStatusChangedEvent(
                saved.getId(), saved.getBookId(), saved.getRequesterId(), saved.getOwnerId(),
                saved.getDesiredDurationDays(), previous, saved.getStatus()));
        return BookRequestResponse.from(saved);
    }
}
