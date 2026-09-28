package com.healthcare.userservice.service;

import com.healthcare.userservice.audit.AuditAction;
import com.healthcare.userservice.audit.AuditOutcome;
import com.healthcare.userservice.audit.AuditService;
import com.healthcare.userservice.dto.AuthResponse;
import com.healthcare.userservice.dto.LoginRequest;
import com.healthcare.userservice.dto.RegisterRequest;
import com.healthcare.userservice.entity.Role;
import com.healthcare.userservice.entity.User;
import com.healthcare.userservice.exception.UserAlreadyExistsException;
import com.healthcare.userservice.repository.UserRepository;
import com.healthcare.userservice.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("dr.smith");
        request.setPassword("secret123");
        request.setEmail("dr.smith@hospital.org");
        request.setRole(Role.DOCTOR);
        return request;
    }

    @Test
    void registerEncodesPasswordAndReturnsToken() {
        when(userRepository.existsByUsername("dr.smith")).thenReturn(false);
        when(userRepository.existsByEmail("dr.smith@hospital.org")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed");
        when(jwtUtil.generateToken("dr.smith", "DOCTOR")).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).isEqualTo("hashed");
        assertThat(saved.getValue().getRole()).isEqualTo(Role.DOCTOR);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.DOCTOR);
        verify(auditService).record(eq("dr.smith"), eq(AuditAction.USER_REGISTERED),
                eq(AuditOutcome.SUCCESS), anyString());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("dr.smith")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByUsername("dr.smith")).thenReturn(false);
        when(userRepository.existsByEmail("dr.smith@hospital.org")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void loginReturnsTokenAndAuditsSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nurse.amy");
        request.setPassword("secret123");

        when(authenticationManager.authenticate(any())).thenReturn(mockAuthentication());
        when(userRepository.findByUsername("nurse.amy")).thenReturn(Optional.of(
                User.builder().id(2L).username("nurse.amy").role(Role.NURSE).build()));
        when(jwtUtil.generateToken("nurse.amy", "NURSE")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo(Role.NURSE);
        verify(auditService).record(eq("nurse.amy"), eq(AuditAction.LOGIN),
                eq(AuditOutcome.SUCCESS), isNull());
    }

    @Test
    void loginAuditsFailureAndThrows() {
        LoginRequest request = new LoginRequest();
        request.setUsername("nurse.amy");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");

        verify(auditService).record(eq("nurse.amy"), eq(AuditAction.LOGIN),
                eq(AuditOutcome.FAILURE), anyString());
    }

    private Authentication mockAuthentication() {
        return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "nurse.amy", "secret123");
    }
}
