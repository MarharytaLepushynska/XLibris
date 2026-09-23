package com.group.xlibris.bookRequest.service;

import com.group.xlibris.bookRequest.command.CreateBookRequestCommand;
import com.group.xlibris.bookRequest.command.UpdateBookRequestCommand;
import com.group.xlibris.bookRequest.dto.BookRequestResponse;
import com.group.xlibris.landCommon.BookRequestStatus;

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
