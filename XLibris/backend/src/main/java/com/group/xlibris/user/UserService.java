package com.group.xlibris.user;

import com.group.xlibris.user.internal.CreateUserCommand;
import com.group.xlibris.user.internal.UpdateUserCommand;
import com.group.xlibris.user.internal.UserUpdateAdminCommand;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserResponse getUserById(UUID id);
    List<UserResponse> getAllUsers(String name, int page, int size);
    UserContactInfo getContactInfoById(UUID targetUserId, UUID viewerId);
    UserResponse createUser(CreateUserCommand command);
    UserResponse updateUser(UUID id, UpdateUserCommand command);
    UserResponse updateUserAdmin(UUID id, UUID callerId, UserUpdateAdminCommand command);
    void removeUser(UUID id);
}
