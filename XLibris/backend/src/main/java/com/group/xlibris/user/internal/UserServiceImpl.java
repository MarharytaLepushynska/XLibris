package com.group.xlibris.user.internal;

import com.group.xlibris.common.IdMismatch;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.LoanService;
import com.group.xlibris.user.UserService;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserResponse;
import com.group.xlibris.user.Role;
import com.group.xlibris.common.AccessDeniedException;
import com.group.xlibris.user.ContactAccessDeniedException;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final LoanService loanService;

    public UserServiceImpl(UserRepository userRepository, LoanService loanService) {
        this.userRepository = userRepository;
        this.loanService = loanService;
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
        boolean hasConfirmedLoan = !loanService
                .getAllLoans(targetUserId, viewerId, null)
                .isEmpty();

        if (!hasConfirmedLoan) {
            throw new ContactAccessDeniedException("No confirmed loan");
        }

        return new UserContactInfo(user.getEmail(), user.getPhone());
    }

    @Override
    public UserResponse createUser(CreateUserCommand command) {
        User user = User.create(command);
        User saved = userRepository.save(user);
        System.out.println("User with id " + user.getId() + "was created");
        return UserResponse.from(saved);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserCommand command) {
        if (!id.equals(command.id())) {
            throw new IdMismatch("Id mismatch");
        }

        User user = findUserOrThrow(id);
        user.updateDetails(command);
        User updated = userRepository.save(user);
        System.out.println("User information with id " + updated.getId() + " was updated");
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
        System.out.println("User information with id " + updated.getId() + " was updated by admin");
        return UserResponse.from(updated);
    }

    @Override
    public void removeUser(UUID id) {
        if(!userRepository.existsById(id)) {
            throw new NotFoundException("User (id= " + id + ") was not found");
        }
        userRepository.deleteById(id);
        System.out.println("User with id " + id + " was deleted");
    }

    private User findUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User (id= " + id + ") was not found"));
    }
}
