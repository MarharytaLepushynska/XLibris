package com.group.xlibris.user;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import com.group.xlibris.user.dto.AdminUserUpdateRequest;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserRequest;
import com.group.xlibris.user.dto.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll(@RequestParam(required = false) String name,
                                                     @RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        List<UserResponse> responseList = userService.getAllUsers(name, page, size);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}/contact")
    public ResponseEntity<UserContactInfo> getContactInfoById(@PathVariable UUID id, @RequestParam UUID viewerId) {
        //TODO: замінити коли буде автентифікація
        return ResponseEntity.ok(userService.getContactInfoById(id, viewerId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Validated(OnCreate.class) @RequestBody UserRequest request){
        UserResponse response = userService.createUser(request.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestParam UUID requesterId, @Validated(OnUpdate.class) @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request.toUpdateCommand(), requesterId));
    }

    @PutMapping("/{id}/admin")
    public ResponseEntity<UserResponse> updateAdmin(@PathVariable UUID id,
                                                    @RequestParam UUID callerId,
                                                    @Valid @RequestBody AdminUserUpdateRequest request) {
        //TODO: замінити коли буде автентифікація
        return ResponseEntity.ok(userService.updateUserAdmin(id, callerId, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id, @RequestParam UUID callerId) {
        userService.removeUser(id, callerId);
        return ResponseEntity.noContent().build();
    }
}
