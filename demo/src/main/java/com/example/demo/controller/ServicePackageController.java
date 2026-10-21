package com.example.demo.controller;

import com.example.demo.dto.ServicePackageRequest;
import com.example.demo.entity.ServicePackage;
import com.example.demo.service.ServicePackageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/packages")
public class ServicePackageController {

    private final ServicePackageService service;

    public ServicePackageController(ServicePackageService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ServicePackage> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        return service.getPackages(keyword, status, page, size);
    }

    @GetMapping("/{id}")
    public ServicePackage getDetail(@PathVariable Long id) {
        return service.getPackageById(id);
    }

    @PostMapping
    public ResponseEntity<ServicePackage> createPackage(@Valid @RequestBody ServicePackageRequest request) {
        ServicePackage createdPackage = service.createPackage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPackage);
    }

    @PutMapping("/{id}")
    public ServicePackage updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody ServicePackageRequest request
    ) {
        return service.updatePackage(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        service.deletePackage(id);
        return ResponseEntity.noContent().build();
    }
}
