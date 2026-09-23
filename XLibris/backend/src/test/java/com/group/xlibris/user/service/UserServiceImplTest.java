package com.group.xlibris.user.service;

import com.group.xlibris.common.IdMismatch;
import com.group.xlibris.common.NotFoundException;
import com.group.xlibris.loan.dto.LoanResponse;
import com.group.xlibris.loan.entity.Loan;
import com.group.xlibris.loan.service.LoanService;
import com.group.xlibris.user.internal.*;
import com.group.xlibris.user.dto.UserContactInfo;
import com.group.xlibris.user.dto.UserResponse;
import com.group.xlibris.user.Role;
import com.group.xlibris.common.AccessDeniedException;
import com.group.xlibris.user.ContactAccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoanService loanService;

    private UserServiceImpl userService;

    private UUID userId;
    private UUID userId2;
    private UUID adminId;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, loanService);

        userId = UUID.randomUUID();
        userId2 = UUID.randomUUID();
        adminId = UUID.randomUUID();

        user = new User(userId, "Artem", "Lviv", null, "a@gmail.com",
                "+380998876443", Instant.now(), Role.USER,
                1.0, 0.9, 0, 0, 0);
    }

    @Test
    void shouldGetByIdSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);
        assertNotNull(response);
        assertEquals("Artem", response.name());
        assertEquals("Lviv", response.city());
        assertEquals(Role.USER, response.role());
        assertEquals(1.0, response.ownerRating());
        assertEquals(0.9, response.borrowerRating());
        assertEquals(0, response.successfulOwnerLoans());
        assertEquals(0, response.successfulBorrowerLoans());
        assertEquals(0, response.overdueReturnsCount());

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldTrowNotFoundWhenUserMissing() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void shouldFilterUsersByNameSuccessfully() {
        User user2 = new User(userId2, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserResponse> responses = userService.getAllUsers("tem", 0, 20);

        assertEquals(1, responses.size());
        assertEquals("Artem", responses.get(0).name());

        verify(userRepository).findAll();
    }

    @Test
    void shouldApplyPaginationSuccessfully() {
        User user2 = new User(userId2, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);

        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserResponse> responsesPage1 = userService.getAllUsers(null, 0, 1);
        List<UserResponse> responsesPage2 = userService.getAllUsers(null, 1, 1);

        assertEquals(1, responsesPage1.size());
        assertEquals(1, responsesPage2.size());
        assertNotEquals(responsesPage1.get(0).id(), responsesPage2.get(0).id());

        verify(userRepository, times(2)).findAll();
    }

    @Test
    void shouldGetContactInfoByIdSuccessfully() {
        Loan loan = Loan.create(UUID.randomUUID(), userId, userId2, Instant.now().plusSeconds(1200));

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(loanService.getAllLoans(userId, userId2, null))
                .thenReturn(List.of(LoanResponse.from(loan)));

        UserContactInfo response = userService.getContactInfoById(userId, userId2);
        assertEquals("a@gmail.com", response.email());
        assertEquals("+380998876443", response.phone());

        verify(userRepository).findById(userId);
        verify(loanService).getAllLoans(userId, userId2, null);
    }

    @Test
    void shouldDenyContactInfoWithoutLoan() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(loanService.getAllLoans(userId, userId2, null))
                .thenReturn(List.of());

        assertThrows(ContactAccessDeniedException.class, () -> userService.getContactInfoById(userId, userId2));
        verify(userRepository).findById(userId);
        verify(loanService).getAllLoans(userId, userId2, null);
    }

    @Test
    void shouldCreateUserSuccessfully() {
        CreateUserCommand command = new CreateUserCommand("Max", "Kyiv", null,"m@gamil.com", "+380663546524");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.createUser(command);
        assertEquals("Max", response.name());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserSuccessfully() {
        UpdateUserCommand command = new UpdateUserCommand(userId, "Dima", "Berlin", null, "d@gmail.com", "+380776654334");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUser(userId, command);
        assertEquals("Dima", response.name());
        assertEquals("Berlin", response.city());

        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldTrowIdMismatchWhenUpdatingWithWrongId() {
        UpdateUserCommand command = new UpdateUserCommand(userId2, "Dima", "Berlin", null, "d@gmail.com", "+380776654334");
        assertThrows(IdMismatch.class, () -> userService.updateUser(userId, command));
        verify(userRepository, never()).findById(any());

    }

    @Test
    void shouldAdminUpdateSuccessfully() {
        User admin = new User(adminId, "Admin", "Kyiv", null, "a@gmail.com",
                "+380998876446", Instant.now(), Role.ADMIN,
                null, null, 0, 0, 0);

        UserUpdateAdminCommand command = new UserUpdateAdminCommand(userId, "Ivan", "Rivne", null, "i@gmail.com",
                "+380778865443", Role.USER, 1.4, 6.7, 6, 6, 0);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserAdmin(userId, adminId, command);

        assertEquals("Ivan", response.name());
        assertEquals(6.7, response.borrowerRating());

        verify(userRepository).findById(adminId);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldDenyAdminUpdateWhenNotAdmin() {
        User user2 = new User(userId2, "Marta", "Kyiv", null, "m@gmail.com",
                "+380998876446", Instant.now(), Role.USER,
                2.0, 1.9, 4, 5, 1);

        UserUpdateAdminCommand command = new UserUpdateAdminCommand(userId, "Ivan", "Rivne", null, "i@gmail.com",
                "+380778865443", Role.USER, 1.4, 6.7, 6, 6, 0);

        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));

        assertThrows(AccessDeniedException.class, () -> userService.updateUserAdmin(userId, userId2, command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(userId)).thenReturn(true);
        userService.removeUser(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void shouldTrowNotFoundExceptionWhenNoDeletingUser() {
        when(userRepository.existsById(userId)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.removeUser(userId));
        verify(userRepository, never()).deleteById(any());
    }
}