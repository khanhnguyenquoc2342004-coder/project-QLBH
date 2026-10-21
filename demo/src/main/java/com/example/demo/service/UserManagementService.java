package com.example.demo.service;

import com.example.demo.dto.AdminUserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.dto.UserUpdateRequest;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.UserRole;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserManagementService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(AppUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByIdAscending().stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserResponse createUser(AdminUserCreateRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());
        ensureUsernameAndEmailAreAvailable(username, email);

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEnabled(true);

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request, String currentUsername) {
        AppUser user = findUser(id);
        String email = normalize(request.email());

        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email đã được sử dụng");
        }
        if (user.getUsername().equalsIgnoreCase(currentUsername) && !request.enabled()) {
            throw new BadRequestException("Không thể khóa chính tài khoản đang đăng nhập");
        }
        if (user.getUsername().equalsIgnoreCase(currentUsername) && user.getRole() != request.role()) {
            throw new BadRequestException("Không thể tự thay đổi vai trò của chính mình");
        }

        ensureAtLeastOneActiveAdminRemains(user, request.role(), request.enabled());

        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setRole(request.role());
        user.setEnabled(request.enabled());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    private AppUser findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng có id: " + id));
    }

    private void ensureUsernameAndEmailAreAvailable(String username, String email) {
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Tên đăng nhập đã được sử dụng");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email đã được sử dụng");
        }
    }

    private void ensureAtLeastOneActiveAdminRemains(AppUser user, UserRole nextRole, boolean nextEnabled) {
        boolean isActiveAdmin = user.getRole() == UserRole.ADMIN && user.isEnabled();
        boolean remainsActiveAdmin = nextRole == UserRole.ADMIN && nextEnabled;

        if (isActiveAdmin && !remainsActiveAdmin && userRepository.countByRoleAndEnabledTrue(UserRole.ADMIN) <= 1) {
            throw new BadRequestException("Hệ thống phải luôn có ít nhất một quản trị viên đang hoạt động");
        }
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getAddress(),
                user.getRole().name(),
                user.isEnabled()
        );
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }
}
