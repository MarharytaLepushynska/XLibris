package com.group.xlibris.bookRequest.internal;

import com.group.xlibris.book.Book;
import com.group.xlibris.book.dto.BookResponse;
import com.group.xlibris.book.BookStatus;
import com.group.xlibris.book.BookService;

import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.bookRequest.BookRequestService;
import com.group.xlibris.common.BookRequestStatus;
import com.group.xlibris.bookRequest.BookRequestStatusChangedEvent;
import com.group.xlibris.bookRequest.DuplicateBookRequestException;
import com.group.xlibris.bookRequest.InvalidBookRequestStateException;
import com.group.xlibris.bookRequest.InvalidBookStateException;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.common.AccessDeniedException;
import com.group.xlibris.user.User;
import com.group.xlibris.user.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BookRequestServiceImpl implements BookRequestService {
    private final BookRequestRepository repository;
    private final BookService bookService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    private static final Logger log = LoggerFactory.getLogger(BookRequestServiceImpl.class);

    public BookRequestServiceImpl(BookRequestRepository repository,
                                  BookService bookService,
                                  ApplicationEventPublisher eventPublisher,
                                  UserService userService) {
        this.repository = repository;
        this.bookService = bookService;
        this.eventPublisher = eventPublisher;
        this.userService = userService;
    }

    @Override
    public BookRequestResponse create(CreateBookRequestCommand command) {
        BookResponse book = bookService.getBookById(command.bookId());
        if (book.ownerId().equals(command.requesterId())) {
            throw new IllegalArgumentException("Cannot request your own book");
        }
        if (book.status() == BookStatus.BLOCKED) {
            throw new InvalidBookStateException("Book is blocked");
        }

        User requester = userService.getEntityById(command.requesterId());
        Book reqBook = bookService.getEntityById(command.bookId());
        User owner = reqBook.getOwner();

        boolean existsAlready = repository.existsByBook_IdAndRequester_IdAndStatusIn(command.bookId(), command.requesterId(),
                List.of(BookRequestStatus.PENDING, BookRequestStatus.APPROVED));
        if (existsAlready) {
            throw new DuplicateBookRequestException("Request for this book was already created");
        }

        BookRequestEntity entity = new BookRequestEntity(
                UUID.randomUUID(),
                reqBook,
                requester,
                owner,
                command.desiredDurationDays(),
                BookRequestStatus.PENDING,
                Instant.now(),
                null
        );
        BookRequestEntity saved = repository.save(entity);
        log.info("Book request for book with id={} was created", saved.getBook().getId());
        return BookRequestResponse.from(saved);
    }

    @Override
    public BookRequestResponse getById(UUID id) {

        log.debug("Finding book request by id={}", id);

        return BookRequestResponse.from(findOrThrow(id));
    }

    @Override
    public List<BookRequestResponse> getAll(UUID bookId, UUID requesterId, BookRequestStatus status, int page, int size) {

        log.debug("Fetching book requests: bookId={}, requesterId={}, status={}, page={}, size={}", bookId, requesterId, status, page, size);

        Pageable pageble = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return repository.findWithDetails(bookId, requesterId, status, pageble)
                .stream()
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

        BookResponse book = bookService.getBookById(existing.getBook().getId());
        checkActor(existing, book, command.actorId(), target);

        if (target == BookRequestStatus.APPROVED || target == BookRequestStatus.FULFILLED) {
            if (book.status() != BookStatus.AVAILABLE) {
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
        log.info("Book request with id={} was deleted", id);
    }

    @Override
    public void cancelOpenRequests(UUID bookId) {
        for (BookRequestEntity request : getOpenRequests(bookId)) {
            saveStatus(request, BookRequestStatus.CANCELLED);
        }
        log.info("Book requests for book with id={} were cancelled", bookId);
    }

    private BookRequestEntity findOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book request (id= " + id + ") was not found"));
    }

    private List<BookRequestEntity> getOpenRequests(UUID bookId) {
        return repository.findByBook_IdAndStatusInOrderByCreatedAtAsc(bookId, List.of(BookRequestStatus.PENDING,
                BookRequestStatus.APPROVED));
    }

    private void checkFirstInQueue(BookRequestEntity request) {
        List<BookRequestEntity> queue = getOpenRequests(request.getBook().getId());
        if (queue.isEmpty() || !queue.getFirst().getId().equals(request.getId())) {
            throw new InvalidBookRequestStateException("Only the first request can be confirmed");
        }
    }

    private void checkActor(BookRequestEntity request, BookResponse book,
                            UUID actorId, BookRequestStatus target) {
        boolean allowed = switch (target) {
            case APPROVED, REJECTED -> book.ownerId().equals(actorId);
            case FULFILLED, CANCELLED -> request.getRequester().getId().equals(actorId);
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
        log.info("Status of book request with id={} was changed from {} to {}", request.getId(), previous, target);

        eventPublisher.publishEvent(new BookRequestStatusChangedEvent(
                saved.getId(), saved.getBook().getId(), saved.getRequester().getId(), saved.getOwner().getId(),
                saved.getDesiredDurationDays(), previous, saved.getStatus()));
        log.info("Event for changing book request status was published");
        return BookRequestResponse.from(saved);
    }
}
