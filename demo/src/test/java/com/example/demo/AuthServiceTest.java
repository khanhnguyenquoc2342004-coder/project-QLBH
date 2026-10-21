package com.example.demo;

import com.example.demo.dto.AuthUserResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, true);
    }

    @Test
    void firstRegisteredUser_becomesAdminAndGetsHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                "Nguyễn Văn A",
                "admin",
                "admin@example.com",
                "Password123"
        );
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("Password123")).thenReturn("hashed-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthUserResponse response = authService.register(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(UserRole.ADMIN, userCaptor.getValue().getRole());
        assertEquals("hashed-password", userCaptor.getValue().getPasswordHash());
        assertNotEquals("Password123", userCaptor.getValue().getPasswordHash());
        assertEquals("ADMIN", response.role());
    }
}
