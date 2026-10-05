package com.group.xlibris.user.internal;

import com.group.xlibris.common.IdMismatch;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.user.*;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserResponse;
import com.group.xlibris.common.AccessDeniedException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserLoanCheck userLoanCheck;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public UserServiceImpl(UserRepository userRepository, UserLoanCheck userLoanCheck) {
        this.userRepository = userRepository;
        this.userLoanCheck = userLoanCheck;
    }

    @Override
    public UserResponse getUserById(UUID id) {

        log.debug("Finding user by id={}", id);

        User user = findUserOrThrow(id);
        return UserResponse.from(user);
    }

    @Override
    public User getUserReferenceById(UUID id) {
        return userRepository.getReferenceById(id);
    }

    @Override
    public List<UserResponse> getAllUsers(String name, int page, int size) {

        log.debug("Fetching users: name={}, page={}, size={}", name, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        List<User> users = name == null
                ? userRepository.findAll(pageable).getContent()
                : userRepository.findByNameContainingIgnoreCase(name, pageable);

        return users.stream()
                .map(UserResponse::from)
                .toList();
    }

    @Override
    public UserContactInfo getContactInfoById(UUID targetUserId, UUID viewerId) {

        log.debug("Checking contact information access: targetUserId={}, viewerId={}", targetUserId, viewerId);

        User user = findUserOrThrow(targetUserId);
        if (!userLoanCheck.canViewContacts(targetUserId, viewerId)) {
            throw new ContactAccessDeniedException("No confirmed loan");
        }

        return new UserContactInfo(user.getEmail(), user.getPhone());
    }

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        User user = User.create(command);
        User saved = userRepository.save(user);
        log.info("User with id={} was created", saved.getId());
        return UserResponse.from(saved);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserCommand command, UUID requesterId) {
        if (!id.equals(command.id())) {
            throw new IdMismatch("Id mismatch");
        }

        if (!id.equals(requesterId)) {
            throw new AccessDeniedException("User can only change their information");
        }

        User user = findUserOrThrow(id);
        user.updateDetails(command);
        User updated = userRepository.save(user);
        log.info("User information with id={} was updated", updated.getId());
        return UserResponse.from(updated);
    }

    @Override
    public UserResponse updateUserAdmin(UUID id, UUID callerId, UserUpdateAdminCommand command) {
        if (!id.equals(command.id())) {
            throw new IdMismatch("Id mismatch");
        }

        User caller = findUserOrThrow(callerId);
        if(caller.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can update roles and ratings");
        }

        User user = findUserOrThrow(id);
        user.updateDetails(command);
        User updated = userRepository.save(user);
        log.info("User information with id={} was updated by admin", updated.getId());
        return UserResponse.from(updated);
    }

    @Override
    public void removeUser(UUID id, UUID callerId) {
        if(!userRepository.existsById(id)) {
            throw new NotFoundException("User (id= " + id + ") was not found");
        }

        User caller = findUserOrThrow(callerId);
        if (!id.equals(callerId) && caller.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("User can only change their information");
        }

        if (userLoanCheck.hasActiveLoans(id)) {
            throw new UserHasActiveLoansException("User has active loans and cannot be deleted");
        }

        userRepository.deleteById(id);
        log.info("User with id={} was deleted", id);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User (id= " + id + ") was not found"));
    }

    @Override
    public User getEntityById(UUID id) {
        return findUserOrThrow(id);
    }
}
