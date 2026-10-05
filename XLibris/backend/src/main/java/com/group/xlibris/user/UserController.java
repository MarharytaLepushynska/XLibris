package com.group.xlibris.user;

import com.group.xlibris.common.OnCreate;
import com.group.xlibris.common.OnUpdate;
import com.group.xlibris.user.dto.AdminUserUpdateRequest;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserRequest;
import com.group.xlibris.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Users", description = "Endpoints for managing user accounts")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves detailed profile information about a specific user by their id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of users with optional filtering by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users successfully retrieved")
    })
    public ResponseEntity<List<UserResponse>> getAll(@RequestParam(required = false) String name,
                                                     @RequestParam(defaultValue = "0") @Min(0) int page,
                                                     @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        List<UserResponse> responseList = userService.getAllUsers(name, page, size);
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}/contact")
    @Operation(summary = "Get user contact info", description = "Retrieves private contact info (email and phone) if a confirmed loan exists between viewer and target user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact information successfully retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied: no confirmed loan between participants"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserContactInfo> getContactInfoById(@PathVariable UUID id, @RequestParam UUID viewerId) {
        //TODO: замінити коли буде автентифікація
        return ResponseEntity.ok(userService.getContactInfoById(id, viewerId));
    }

    @PostMapping
    @Operation(summary = "Create user", description = "Registers a new user profile in the system")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully created"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid input data")
    })
    public ResponseEntity<UserResponse> create(@Validated(OnCreate.class) @RequestBody UserRequest request){
        UserResponse response = userService.createUser(request.toCommand());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates details of an existing user profile (allowed only by the user themselves)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile successfully updated"),
            @ApiResponse(responseCode = "400", description = "Validation error or id mismatch"),
            @ApiResponse(responseCode = "403", description = "Access denied: users can only change their own information"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestParam UUID requesterId, @Validated(OnUpdate.class) @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request.toUpdateCommand(), requesterId));
    }

    @PutMapping("/{id}/admin")
    @Operation(summary = "Update user as admin", description = "Allows an administrator to update user roles, ratings and stats")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile successfully updated by admin"),
            @ApiResponse(responseCode = "400", description = "Validation error or id mismatch"),
            @ApiResponse(responseCode = "403", description = "Access denied: only administrators can update roles and ratings"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateAdmin(@PathVariable UUID id,
                                                    @RequestParam UUID callerId,
                                                    @Valid @RequestBody AdminUserUpdateRequest request) {
        //TODO: замінити коли буде автентифікація
        return ResponseEntity.ok(userService.updateUserAdmin(id, callerId, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Removes a user record if they have no active loans")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Conflict: user has active loans and cannot be deleted"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteById(@PathVariable UUID id, @RequestParam UUID callerId) {
        userService.removeUser(id, callerId);
        return ResponseEntity.noContent().build();
    }
}
