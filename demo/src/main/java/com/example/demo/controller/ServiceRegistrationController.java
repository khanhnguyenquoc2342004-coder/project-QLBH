package com.example.demo.controller;

import com.example.demo.dto.ServiceRegistrationRequest;
import com.example.demo.dto.ServiceRegistrationResponse;
import com.example.demo.dto.PurchasedServiceResponse;
import com.example.demo.entity.RegistrationStatus;
import com.example.demo.service.ServiceRegistrationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class ServiceRegistrationController {

    private final ServiceRegistrationService registrationService;

    public ServiceRegistrationController(ServiceRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    public ResponseEntity<ServiceRegistrationResponse> registerAndActivate(
            @Valid @RequestBody ServiceRegistrationRequest request,
            Authentication authentication
    ) {
        ServiceRegistrationResponse response = registrationService.registerAndActivate(authentication.getName(), request);
        if (response.status() == RegistrationStatus.ACTIVATED) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        if ("EXTERNAL_TIMEOUT".equals(response.errorCode())) {
            return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(response);
        }
        return ResponseEntity.status(422).body(response);
    }

    @GetMapping("/my")
    public List<ServiceRegistrationResponse> getMyRegistrations(Authentication authentication) {
        return registrationService.getMyRegistrations(authentication.getName());
    }

    @GetMapping("/purchased-services")
    public List<PurchasedServiceResponse> getPurchasedServices(Authentication authentication) {
        return registrationService.getPurchasedServices(authentication.getName());
    }
}
