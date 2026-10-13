package com.thanh.fastfood.auth.service;

import com.thanh.fastfood.auth.dto.LoginRequest;
import com.thanh.fastfood.auth.dto.RegisterRequest;
import com.thanh.fastfood.auth.entity.Role;
import com.thanh.fastfood.auth.entity.User;
import com.thanh.fastfood.auth.entity.UserRole;
import com.thanh.fastfood.auth.repository.RoleRepository;
import com.thanh.fastfood.auth.repository.UserRepository;
import com.thanh.fastfood.common.security.JwtService;
import com.thanh.fastfood.common.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private Role customerRole;

    @BeforeEach
    void setUp() {
        customerRole = Role.builder()
                .id(1L)
                .name(UserRole.CUSTOMER)
                .build();
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@gmail.com");
        request.setPassword("Password@123");
        request.setFullName("Test User");

        when(userRepository.existsByEmail("admin@gmail.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> authService.register(request)
        );

        verify(userRepository).existsByEmail("admin@gmail.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_shouldSaveUser_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("  customer@gmail.com  ");
        request.setPassword("Password@123");
        request.setFullName(" Test Customer ");

        when(userRepository.existsByEmail("customer@gmail.com"))
                .thenReturn(false);

        when(roleRepository.findByName(UserRole.CUSTOMER))
                .thenReturn(Optional.of(customerRole));

        when(passwordEncoder.encode("Password@123"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(10L);
                    return user;
                });

        var response = authService.register(request);

        assertEquals(10L, response.getId());
        assertEquals("customer@gmail.com", response.getEmail());
        assertEquals("Test Customer", response.getFullName());
        assertEquals("CUSTOMER", response.getRole());

        verify(passwordEncoder).encode("Password@123");
        verify(userRepository).save(any(User.class));
    }


    @Test
    void login_shouldReturnToken_whenCredentialsAreCorrect() {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@gmail.com");
        request.setPassword("Password@123");

        User user = User.builder()
                .id(10L)
                .email("customer@gmail.com")
                .password("hashed-password")
                .fullName("Test Customer")
                .role(customerRole)
                .build();

        when(userRepository.findByEmail("customer@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password@123",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("fake-jwt-token");

        when(jwtService.getExpirationMs())
                .thenReturn(900000L);

        var response = authService.login(request);

        assertEquals("fake-jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());

        verify(jwtService).generateToken(user);
    }

    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {
        LoginRequest request = new LoginRequest();
        request.setEmail("customer@gmail.com");
        request.setPassword("WrongPassword@123");

        User user = User.builder()
                .id(10L)
                .email("customer@gmail.com")
                .password("hashed-password")
                .fullName("Test Customer")
                .role(customerRole)
                .build();

        when(userRepository.findByEmail("customer@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword@123",
                "hashed-password"
        )).thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void login_shouldThrowException_whenEmailDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never()).generateToken(any(User.class));
    }

}
