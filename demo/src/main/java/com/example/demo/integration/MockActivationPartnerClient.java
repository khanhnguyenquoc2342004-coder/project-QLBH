package com.example.demo.integration;

import com.example.demo.dto.ActivationSimulation;
import org.springframework.stereotype.Component;

@Component
public class MockActivationPartnerClient implements ActivationPartnerClient {

    @Override
    public ActivationPartnerResponse activate(ActivationPartnerCommand command) {
        if (command.simulation() == ActivationSimulation.BUSINESS_ERROR) {
            throw new PartnerBusinessException("Doi tac tu choi kich hoat goi theo quy tac nghiep vu");
        }
        if (command.simulation() == ActivationSimulation.TIMEOUT) {
            throw new PartnerTimeoutException("Doi tac khong phan hoi trong thoi gian cho phep");
        }

        String suffix = command.requestId().replace("-", "");
        return new ActivationPartnerResponse("MOCK-EWS-" + suffix.substring(0, Math.min(12, suffix.length())));
    }
}
