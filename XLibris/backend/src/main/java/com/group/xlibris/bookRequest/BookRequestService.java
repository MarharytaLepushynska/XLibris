package com.group.xlibris.bookRequest;

import com.group.xlibris.bookRequest.internal.CreateBookRequestCommand;
import com.group.xlibris.bookRequest.internal.UpdateBookRequestCommand;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.common.BookRequestStatus;

import java.util.List;
import java.util.UUID;

public interface BookRequestService {
    BookRequestResponse create(CreateBookRequestCommand command);
    BookRequestResponse getById(UUID id);
    List<BookRequestResponse> getAll(UUID bookId, UUID requesterId, BookRequestStatus status, int page, int size);
    BookRequestResponse updateStatus(UpdateBookRequestCommand command);
    void delete(UUID id);
    void cancelOpenRequests(UUID bookId);
}
