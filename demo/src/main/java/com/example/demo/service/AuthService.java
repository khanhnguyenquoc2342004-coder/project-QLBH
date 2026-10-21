package com.example.demo.service;

import com.example.demo.dto.AuthUserResponse;
import com.example.demo.dto.CustomerProfileRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean firstRegisteredUserAdmin;

    public AuthService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.first-registered-user-admin:true}") boolean firstRegisteredUserAdmin
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.firstRegisteredUserAdmin = firstRegisteredUserAdmin;
    }

    @Transactional
    public AuthUserResponse register(RegisterRequest request) {
        String username = request.username().trim().toLowerCase();
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Tên đăng nhập đã được sử dụng");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email đã được sử dụng");
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(firstRegisteredUserAdmin && userRepository.count() == 0 ? UserRole.ADMIN : UserRole.CUSTOMER);

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public AuthUserResponse getCurrentUser(String username) {
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));

        return toResponse(user);
    }

    @Transactional
    public AuthUserResponse updateProfile(String username, CustomerProfileRequest request) {
        AppUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));
        user.setFullName(request.fullName().trim());
        user.setPhone(hasText(request.phone()) ? request.phone().trim() : null);
        user.setAddress(hasText(request.address()) ? request.address().trim() : null);
        return toResponse(userRepository.save(user));
    }

    private AuthUserResponse toResponse(AppUser user) {
        return new AuthUserResponse(
                user.getId(), user.getUsername(), user.getFullName(), user.getRole().name(),
                user.getEmail(), user.getPhone(), user.getAddress()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
