package com.thanh.fastfood.auth.service;

import com.thanh.fastfood.auth.dto.RegisterRequest;
import com.thanh.fastfood.auth.dto.UserResponse;
import com.thanh.fastfood.auth.entity.Role;
import com.thanh.fastfood.auth.entity.User;
import com.thanh.fastfood.auth.entity.UserRole;
import com.thanh.fastfood.auth.repository.RoleRepository;
import com.thanh.fastfood.auth.repository.UserRepository;
import com.thanh.fastfood.common.exception.DuplicateResourceException;
import com.thanh.fastfood.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException(
                    "Email đã được sử dụng"
            );
        }

        Role customerRole = roleRepository
                .findByName(UserRole.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy role CUSTOMER trong database"
                ));

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .phone(request.getPhone())
                .role(customerRole)
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .phone(savedUser.getPhone())
                .role(savedUser.getRole().getName().name())
                .build();
    }
}