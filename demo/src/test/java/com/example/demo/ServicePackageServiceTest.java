package com.example.demo;

import com.example.demo.dto.ServicePackageRequest;
import com.example.demo.entity.ServicePackage;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.repository.ServicePackageRepository;
import com.example.demo.service.ServicePackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePackageServiceTest {

    @Mock
    private ServicePackageRepository repository;

    private ServicePackageService service;

    @BeforeEach
    void setUp() {
        service = new ServicePackageService(repository);
    }

    @Test
    void createPackage_rejectsExistingCode() {
        ServicePackageRequest request = request("PKG-001", "Gói cơ bản", "ACTIVE");
        when(repository.existsByCodeIgnoreCase("PKG-001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.createPackage(request));

        verify(repository, never()).save(any());
    }

    @Test
    void updatePackage_updatesTheExistingPackage() {
        ServicePackage existingPackage = new ServicePackage();
        existingPackage.setId(1L);
        existingPackage.setCode("PKG-001");
        existingPackage.setName("Gói cũ");
        existingPackage.setPrice(BigDecimal.valueOf(100_000));
        existingPackage.setStatus("ACTIVE");
        when(repository.findById(1L)).thenReturn(Optional.of(existingPackage));
        when(repository.save(existingPackage)).thenReturn(existingPackage);

        ServicePackage updatedPackage = service.updatePackage(
                1L,
                request("PKG-001", "Gói mới", "INACTIVE")
        );

        assertEquals("Gói mới", updatedPackage.getName());
        assertEquals("INACTIVE", updatedPackage.getStatus());
        verify(repository).save(existingPackage);
    }

    private ServicePackageRequest request(String code, String name, String status) {
        return new ServicePackageRequest(
                code,
                name,
                BigDecimal.valueOf(100_000),
                "Dữ liệu test",
                "Dịch vụ",
                null,
                0,
                status
        );
    }
}
