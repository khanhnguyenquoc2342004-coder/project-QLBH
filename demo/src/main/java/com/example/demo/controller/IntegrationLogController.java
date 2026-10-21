package com.example.demo.controller;

import com.example.demo.dto.IntegrationLogResponse;
import com.example.demo.service.ServiceRegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integration-logs")
public class IntegrationLogController {

    private final ServiceRegistrationService registrationService;

    public IntegrationLogController(ServiceRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping
    public List<IntegrationLogResponse> getRecentLogs() {
        return registrationService.getRecentIntegrationLogs();
    }
}
