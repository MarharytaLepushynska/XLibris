package com.group.xlibris.user;

import com.group.xlibris.common.exception.IdMismatch;
import com.group.xlibris.common.exception.NotFoundException;
import com.group.xlibris.common.validation.OnCreate;
import com.group.xlibris.common.validation.OnUpdate;
import com.group.xlibris.user.dto.AdminUserUpdateRequest;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserRequest;
import com.group.xlibris.user.dto.UserResponse;
import com.group.xlibris.user.internal.Role;
import com.group.xlibris.user.internal.UserEntity;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final Map<UUID, UserEntity> users;

    public UserController() {
        this.users = new HashMap<>();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        UserEntity user = users.get(id);
        if(user == null) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        UserResponse response = toResponse(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll(@RequestParam(required = false) String name,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        List<UserResponse> responseList = users.values().stream()
                .filter(u -> name == null || u.getName().toLowerCase().contains(name.toLowerCase()))
                .skip((long) page*size)
                .limit(size)
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}/contact")
    public ResponseEntity<UserContactInfo> getContactInfoById(@PathVariable UUID id) {
        UserEntity user = users.get(id);
        if(user == null) {
            throw new NotFoundException("User with id " + id + " not found");
        }
        UserContactInfo info = new UserContactInfo(user.getEmail(), user.getPhone());
        return ResponseEntity.ok(info);
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Validated(OnCreate.class) @RequestBody UserRequest request){
        UserEntity user = createUser(request);
        users.put(user.getId(), user);

        UserResponse response = toResponse(user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Validated(OnUpdate.class) @RequestBody UserRequest request) {
        if(!id.equals(request.id())) {
            throw new IdMismatch("Id mismatch");
        }

        if(!users.containsKey(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        UserEntity existing = users.get(id);
        UserEntity updated = updateUser(existing, request);

        UserResponse response = toResponse(updated);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/admin")
    public ResponseEntity<UserResponse> updateAdmin(@PathVariable UUID id, @Valid @RequestBody AdminUserUpdateRequest request) {
        if(!id.equals(request.id())) {
            throw new IdMismatch("Id mismatch");
        }

        if(!users.containsKey(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        UserEntity existing = users.get(id);
        UserEntity updated = updateUserByAdmin(existing, request);

        UserResponse response = toResponse(updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id) {
        if(!users.containsKey(id)) {
            throw new NotFoundException("User with id " + id + " not found");
        }

        users.remove(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getCity(),
                entity.getPhotoURL(),
                entity.getRegistrationDate(),
                entity.getRole(),
                entity.getOwnerRating(),
                entity.getBorrowerRating(),
                entity.getSuccessfulOwnerLoans(),
                entity.getSuccessfulBorrowerLoans(),
                entity.getOverdueReturnsCount()
        );
    }

    private UserEntity createUser(UserRequest userRequest) {
        UUID id = UUID.randomUUID();
        return new UserEntity(
                id,
                userRequest.name(),
                userRequest.city(),
                userRequest.photoURL(),
                userRequest.email(),
                userRequest.phone(),
                Instant.now(),
                Role.USER,
                null,
                null,
                0,
                0,
                0
        );
    }

    private UserEntity updateUser(UserEntity existing, UserRequest userRequest) {
        return new UserEntity(
            existing.getId(),
            userRequest.name(),
                userRequest.city(),
                userRequest.photoURL(),
                userRequest.email(),
                userRequest.phone(),
                existing.getRegistrationDate(),
                existing.getRole(),
                existing.getOwnerRating(),
                existing.getBorrowerRating(),
                existing.getSuccessfulOwnerLoans(),
                existing.getSuccessfulBorrowerLoans(),
                existing.getOverdueReturnsCount()
        );
    }

    private UserEntity updateUserByAdmin(UserEntity existing, AdminUserUpdateRequest request) {
        return new UserEntity(
                existing.getId(),
                request.name(),
                request.city(),
                request.photoURL(),
                request.email(),
                request.phone(),
                existing.getRegistrationDate(),
                request.role(),
                request.ownerRating(),
                request.borrowerRating(),
                request.successfulOwnerLoans(),
                request.successfulBorrowerLoans(),
                request.overdueReturnsCount()
        );
    }
}
