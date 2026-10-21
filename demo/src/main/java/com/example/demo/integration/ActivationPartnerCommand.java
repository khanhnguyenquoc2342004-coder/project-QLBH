package com.example.demo.integration;

import com.example.demo.dto.ActivationSimulation;

public record ActivationPartnerCommand(
        String requestId,
        String registrationCode,
        String packageCode,
        String customerUsername,
        ActivationSimulation simulation
) {
}
