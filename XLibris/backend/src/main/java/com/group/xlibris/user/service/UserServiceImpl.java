package com.group.xlibris.user.service;

import com.group.xlibris.common.exception.IdMismatch;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.loan.repository.LoanRepository;
import com.group.xlibris.user.command.CreateUserCommand;
import com.group.xlibris.user.command.UpdateUserCommand;
import com.group.xlibris.user.command.UserUpdateAdminCommand;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserResponse;
import com.group.xlibris.user.entity.User;
import com.group.xlibris.user.enums.Role;
import com.group.xlibris.user.exception.AccessDeniedException;
import com.group.xlibris.user.exception.ContactAccessDeniedException;
import com.group.xlibris.user.repository.UserRepository;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final LoanRepository loanRepository;

    public UserServiceImpl(UserRepository userRepository, LoanRepository loanRepository) {
        this.userRepository = userRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    public UserResponse getUserById(UUID id) {
        User user = findUserOrThrow(id);
        return UserResponse.from(user);
    }

    @Override
    public List<UserResponse> getAllUsers(String name, int page, int size) {
        return userRepository.findAll().stream()
                .filter(u -> name == null || u.getName().toLowerCase().contains(name.toLowerCase()))
                .skip((long) page * size)
                .limit(size)
                .map(UserResponse::from)
                .toList();
    }

    @Override
    public UserContactInfo getContactInfoById(UUID targetUserId, UUID viewerId) {
        User user = findUserOrThrow(targetUserId);
        boolean hasConfirmedLoan = loanRepository.findAll().stream()
                .anyMatch(loan ->
                                loan.getOwnerId().equals(targetUserId) && loan.getRenterId().equals(viewerId));
        if (!hasConfirmedLoan) {
            throw new ContactAccessDeniedException("contact info of user " + targetUserId + "is visible only after a confirmed loan");
        }

        return new UserContactInfo(user.getEmail(), user.getPhone());
    }

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        User user = User.create(command);
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserCommand command) {
        if (!id.equals(command.id())) {
            throw new IdMismatch("Id mismatch");
        }

        User user = findUserOrThrow(id);
        user.updateDetails(command);
        return UserResponse.from(userRepository.save(user));
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
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public void removeUser(UUID id) {
        if(!userRepository.existsById(id)) {
            throw new NotFoundException("User (id= " + id + ") was not found");
        }
        userRepository.deleteById(id);
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User (id= " + id + ") was not found"));
    }
}
