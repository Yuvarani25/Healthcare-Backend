package com.healthcare.userservice.service;

import com.healthcare.userservice.audit.AuditAction;
import com.healthcare.userservice.audit.AuditOutcome;
import com.healthcare.userservice.audit.AuditService;
import com.healthcare.userservice.dto.ChangePasswordRequest;
import com.healthcare.userservice.dto.UpdateUserRequest;
import com.healthcare.userservice.dto.UserResponse;
import com.healthcare.userservice.entity.User;
import com.healthcare.userservice.exception.UserAlreadyExistsException;
import com.healthcare.userservice.exception.UserNotFoundException;
import com.healthcare.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return toResponse(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        userRepository.delete(user);
        auditService.record(user.getUsername(), AuditAction.USER_DELETED, AuditOutcome.SUCCESS,
                "Deleted user id " + id);
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        Optional<User> byEmail = userRepository.findByEmail(request.getEmail());
        if (byEmail.isPresent() && !byEmail.get().getId().equals(id)) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        UserResponse response = toResponse(userRepository.save(user));
        auditService.record(user.getUsername(), AuditAction.USER_UPDATED, AuditOutcome.SUCCESS,
                "Email/role updated for user id " + id);
        return response;
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditService.record(username, AuditAction.PASSWORD_CHANGED, AuditOutcome.FAILURE,
                    "Current password did not match");
            throw new BadCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.record(username, AuditAction.PASSWORD_CHANGED, AuditOutcome.SUCCESS, null);
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
