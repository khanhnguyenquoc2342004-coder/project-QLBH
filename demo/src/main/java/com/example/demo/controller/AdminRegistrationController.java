package com.example.demo.controller;

import com.example.demo.dto.ServiceRegistrationResponse;
import com.example.demo.service.ServiceRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/registrations")
public class AdminRegistrationController {

    private final ServiceRegistrationService registrationService;

    public AdminRegistrationController(ServiceRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public List<ServiceRegistrationResponse> getAllRegistrations() {
        return registrationService.getAllRegistrations();
    }
}
