package com.example.demo.service;

import com.example.demo.dto.ServicePackageRequest;
import com.example.demo.entity.ServicePackage;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.ServicePackageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicePackageService {

    private final ServicePackageRepository repository;

    public ServicePackageService(ServicePackageRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ServicePackage> getPackages(String keyword, String status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedKeyword = hasText(keyword) ? keyword.trim() : null;
        String normalizedStatus = hasText(status) ? status.trim().toUpperCase() : null;

        return repository.search(
                normalizedKeyword,
                normalizedStatus,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Transactional(readOnly = true)
    public List<ServicePackage> getAllPackages(String keyword, String status) {
        return getPackages(keyword, status, 0, 100).getContent();
    }

    @Transactional(readOnly = true)
    public ServicePackage getPackageById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói dịch vụ có id: " + id));
    }

    @Transactional
    public ServicePackage createPackage(ServicePackageRequest request) {
        String code = normalizeCode(request.code());

        if (repository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Mã gói dịch vụ đã tồn tại: " + code);
        }

        ServicePackage servicePackage = new ServicePackage();
        applyRequest(servicePackage, request, code);
        return repository.save(servicePackage);
    }

    @Transactional
    public ServicePackage updatePackage(Long id, ServicePackageRequest request) {
        ServicePackage existingPackage = getPackageById(id);
        String code = normalizeCode(request.code());

        if (!existingPackage.getCode().equalsIgnoreCase(code) && repository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateResourceException("Mã gói dịch vụ đã tồn tại: " + code);
        }

        applyRequest(existingPackage, request, code);
        return repository.save(existingPackage);
    }

    @Transactional
    public void deletePackage(Long id) {
        ServicePackage servicePackage = getPackageById(id);
        repository.delete(servicePackage);
    }

    private void applyRequest(ServicePackage target, ServicePackageRequest request, String code) {
        target.setCode(code);
        target.setName(request.name().trim());
        target.setPrice(request.price());
        target.setDescription(hasText(request.description()) ? request.description().trim() : null);
        target.setCategory(hasText(request.category()) ? request.category().trim() : null);
        target.setImageUrl(hasText(request.imageUrl()) ? request.imageUrl().trim() : null);
        target.setDiscountPercent(request.discountPercent() == null ? 0 : request.discountPercent());
        target.setStatus(hasText(request.status()) ? request.status().trim().toUpperCase() : "ACTIVE");
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
