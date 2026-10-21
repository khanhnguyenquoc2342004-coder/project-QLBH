package com.example.demo;

import com.example.demo.dto.ActivationSimulation;
import com.example.demo.dto.ServiceRegistrationRequest;
import com.example.demo.dto.ServiceRegistrationResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.CustomerOrder;
import com.example.demo.entity.CustomerOrderItem;
import com.example.demo.entity.RegistrationStatus;
import com.example.demo.entity.ServicePackage;
import com.example.demo.entity.ServiceRegistration;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.integration.ActivationPartnerClient;
import com.example.demo.integration.ActivationPartnerResponse;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.IntegrationLogRepository;
import com.example.demo.repository.CustomerOrderItemRepository;
import com.example.demo.repository.ServiceRegistrationRepository;
import com.example.demo.service.AuditLogService;
import com.example.demo.service.ServiceRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceRegistrationServiceTest {

    @Mock private ServiceRegistrationRepository registrationRepository;
    @Mock private CustomerOrderItemRepository orderItemRepository;
    @Mock private AppUserRepository userRepository;
    @Mock private IntegrationLogRepository integrationLogRepository;
    @Mock private ActivationPartnerClient activationPartnerClient;
    @Mock private AuditLogService auditLogService;

    private ServiceRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new ServiceRegistrationService(
                registrationRepository,
                orderItemRepository,
                userRepository,
                integrationLogRepository,
                activationPartnerClient,
                auditLogService
        );
    }

    @Test
    void registerAndActivate_successfullyActivatesOnce() {
        AppUser customer = customer();
        CustomerOrderItem orderItem = completedOrderItem(customer);
        when(userRepository.findForUpdateByUsername("customer")).thenReturn(Optional.of(customer));
        when(registrationRepository.findByRequestId("request-0001")).thenReturn(Optional.empty());
        when(orderItemRepository.findById(100L)).thenReturn(Optional.of(orderItem));
        when(registrationRepository.findTopByOrderItemIdOrderByCreatedAtDesc(100L)).thenReturn(Optional.empty());
        when(registrationRepository.saveAndFlush(any(ServiceRegistration.class))).thenAnswer(invocation -> {
            ServiceRegistration registration = invocation.getArgument(0);
            registration.setId(99L);
            registration.setRegistrationCode("DK-TEST-99");
            return registration;
        });
        when(activationPartnerClient.activate(any())).thenReturn(new ActivationPartnerResponse("MOCK-EWS-01"));
        when(registrationRepository.save(any(ServiceRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServiceRegistrationResponse response = service.registerAndActivate(
                "customer",
                new ServiceRegistrationRequest(100L, "request-0001", ActivationSimulation.SUCCESS)
        );

        assertEquals(RegistrationStatus.ACTIVATED, response.status());
        assertEquals("MOCK-EWS-01", response.externalReference());
        verify(activationPartnerClient).activate(any());
    }

    @Test
    void registerAndActivate_replaysExistingRequestWithoutCallingPartnerAgain() {
        AppUser customer = customer();
        ServiceRegistration previous = new ServiceRegistration();
        previous.setId(50L);
        previous.setCustomer(customer);
        previous.setServicePackage(activePackage());
        previous.setOrderItem(completedOrderItem(customer));
        previous.setRequestId("request-0001");
        previous.setRegistrationCode("DK-OLD-50");
        previous.setStatus(RegistrationStatus.ACTIVATED);
        previous.setExternalReference("MOCK-EWS-OLD");
        when(userRepository.findForUpdateByUsername("customer")).thenReturn(Optional.of(customer));
        when(registrationRepository.findByRequestId("request-0001")).thenReturn(Optional.of(previous));

        ServiceRegistrationResponse response = service.registerAndActivate(
                "customer",
                new ServiceRegistrationRequest(100L, "request-0001", ActivationSimulation.SUCCESS)
        );

        assertTrue(response.replayed());
        assertEquals(RegistrationStatus.ACTIVATED, response.status());
        verify(activationPartnerClient, never()).activate(any());
        verify(registrationRepository, never()).saveAndFlush(any());
    }

    @Test
    void registerAndActivate_rejectsPackagePurchasedByAnotherCustomer() {
        AppUser customer = customer();
        AppUser anotherCustomer = customer();
        anotherCustomer.setId(2L);
        CustomerOrderItem foreignOrderItem = completedOrderItem(anotherCustomer);
        when(userRepository.findForUpdateByUsername("customer")).thenReturn(Optional.of(customer));
        when(registrationRepository.findByRequestId("request-0002")).thenReturn(Optional.empty());
        when(orderItemRepository.findById(100L)).thenReturn(Optional.of(foreignOrderItem));

        assertThrows(ResourceNotFoundException.class, () -> service.registerAndActivate(
                "customer",
                new ServiceRegistrationRequest(100L, "request-0002", ActivationSimulation.SUCCESS)
        ));

        verify(activationPartnerClient, never()).activate(any());
    }

    private AppUser customer() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("customer");
        return user;
    }

    private ServicePackage activePackage() {
        ServicePackage servicePackage = new ServicePackage();
        servicePackage.setId(10L);
        servicePackage.setCode("PKG-01");
        servicePackage.setName("Package test");
        servicePackage.setStatus("ACTIVE");
        return servicePackage;
    }

    private CustomerOrderItem completedOrderItem(AppUser customer) {
        CustomerOrder order = new CustomerOrder();
        order.setId(20L);
        order.setOrderCode("DH-000020");
        order.setCustomer(customer);
        order.setStatus("COMPLETED");
        CustomerOrderItem item = new CustomerOrderItem();
        item.setId(100L);
        item.setOrder(order);
        item.setServicePackage(activePackage());
        item.setPackageCode("PKG-01");
        item.setPackageName("Package test");
        item.setQuantity(1);
        return item;
    }
}
