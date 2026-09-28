package com.healthcare.userservice.service;

import com.healthcare.userservice.audit.AuditAction;
import com.healthcare.userservice.audit.AuditOutcome;
import com.healthcare.userservice.audit.AuditService;
import com.healthcare.userservice.dto.ChangePasswordRequest;
import com.healthcare.userservice.dto.UpdateUserRequest;
import com.healthcare.userservice.dto.UserResponse;
import com.healthcare.userservice.entity.Role;
import com.healthcare.userservice.entity.User;
import com.healthcare.userservice.exception.UserAlreadyExistsException;
import com.healthcare.userservice.exception.UserNotFoundException;
import com.healthcare.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User existingUser() {
        return User.builder()
                .id(1L)
                .username("dr.smith")
                .password("hashed")
                .email("dr.smith@hospital.org")
                .role(Role.DOCTOR)
                .build();
    }

    @Test
    void getUserByIdThrowsWhenMissing() {
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(9L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateUserChangesEmailAndRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(userRepository.findByEmail("new@hospital.org")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("new@hospital.org");
        request.setRole(Role.ADMIN);

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getEmail()).isEqualTo("new@hospital.org");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        verify(auditService).record(eq("dr.smith"), eq(AuditAction.USER_UPDATED),
                eq(AuditOutcome.SUCCESS), anyString());
    }

    @Test
    void updateUserRejectsEmailOwnedByAnotherUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));
        when(userRepository.findByEmail("taken@hospital.org")).thenReturn(Optional.of(
                User.builder().id(2L).username("other").build()));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("taken@hospital.org");
        request.setRole(Role.NURSE);

        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void changePasswordEncodesNewPasswordWhenCurrentMatches() {
        when(userRepository.findByUsername("dr.smith")).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("secret123", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newsecret")).thenReturn("new-hash");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("secret123");
        request.setNewPassword("newsecret");

        userService.changePassword("dr.smith", request);

        verify(userRepository).save(any(User.class));
        verify(auditService).record(eq("dr.smith"), eq(AuditAction.PASSWORD_CHANGED),
                eq(AuditOutcome.SUCCESS), isNull());
    }

    @Test
    void changePasswordRejectsWrongCurrentPasswordAndAuditsFailure() {
        when(userRepository.findByUsername("dr.smith")).thenReturn(Optional.of(existingUser()));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newsecret");

        assertThatThrownBy(() -> userService.changePassword("dr.smith", request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).save(any(User.class));
        verify(auditService).record(eq("dr.smith"), eq(AuditAction.PASSWORD_CHANGED),
                eq(AuditOutcome.FAILURE), anyString());
    }

    @Test
    void deleteUserAuditsRemoval() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser()));

        userService.deleteUser(1L);

        verify(userRepository).delete(any(User.class));
        verify(auditService).record(eq("dr.smith"), eq(AuditAction.USER_DELETED),
                eq(AuditOutcome.SUCCESS), anyString());
    }
}
