package com.example.demo.service;

import com.example.demo.dto.IntegrationLogResponse;
import com.example.demo.dto.PurchasedServiceResponse;
import com.example.demo.dto.ServiceRegistrationRequest;
import com.example.demo.dto.ServiceRegistrationResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.CustomerOrderItem;
import com.example.demo.entity.IntegrationLog;
import com.example.demo.entity.RegistrationStatus;
import com.example.demo.entity.ServicePackage;
import com.example.demo.entity.ServiceRegistration;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.integration.ActivationPartnerClient;
import com.example.demo.integration.ActivationPartnerCommand;
import com.example.demo.integration.ActivationPartnerResponse;
import com.example.demo.integration.PartnerBusinessException;
import com.example.demo.integration.PartnerTimeoutException;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.IntegrationLogRepository;
import com.example.demo.repository.CustomerOrderItemRepository;
import com.example.demo.repository.ServiceRegistrationRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceRegistrationService {

    private static final String EXTERNAL_BUSINESS_ERROR = "EXTERNAL_BUSINESS_ERROR";
    private static final String EXTERNAL_TIMEOUT = "EXTERNAL_TIMEOUT";

    private final ServiceRegistrationRepository registrationRepository;
    private final CustomerOrderItemRepository orderItemRepository;
    private final AppUserRepository userRepository;
    private final IntegrationLogRepository integrationLogRepository;
    private final ActivationPartnerClient activationPartnerClient;
    private final AuditLogService auditLogService;

    public ServiceRegistrationService(
            ServiceRegistrationRepository registrationRepository,
            CustomerOrderItemRepository orderItemRepository,
            AppUserRepository userRepository,
            IntegrationLogRepository integrationLogRepository,
            ActivationPartnerClient activationPartnerClient,
            AuditLogService auditLogService
    ) {
        this.registrationRepository = registrationRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
        this.integrationLogRepository = integrationLogRepository;
        this.activationPartnerClient = activationPartnerClient;
        this.auditLogService = auditLogService;
    }

    /**
     * The customer row is locked while checking requestId.  Together with the
     * unique database key this prevents one customer from activating twice on
     * a double click or a resend of the same requestId.
     */
    @Transactional
    public ServiceRegistrationResponse registerAndActivate(
            String username,
            ServiceRegistrationRequest request
    ) {
        AppUser customer = userRepository.findForUpdateByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay tai khoan hien tai"));

        ServiceRegistration existing = registrationRepository.findByRequestId(request.requestId().trim())
                .orElse(null);
        if (existing != null) {
            if (!existing.getCustomer().getId().equals(customer.getId())) {
                throw new BadRequestException("requestId da duoc su dung");
            }
            return toResponse(existing, true);
        }

        CustomerOrderItem orderItem = orderItemRepository.findById(request.orderItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay goi dich vu da mua"));
        if (!orderItem.getOrder().getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Khong tim thay goi dich vu da mua");
        }
        if (!"COMPLETED".equals(orderItem.getOrder().getStatus())) {
            throw new BadRequestException("Chi co the kich hoat goi thuoc don hang da hoan tat");
        }
        ServiceRegistration latest = registrationRepository.findTopByOrderItemIdOrderByCreatedAtDesc(orderItem.getId())
                .orElse(null);
        if (latest != null && latest.getStatus() == RegistrationStatus.ACTIVATED) {
            throw new BadRequestException("Goi dich vu trong don hang nay da duoc kich hoat");
        }
        if (latest != null && latest.getStatus() == RegistrationStatus.PENDING) {
            throw new BadRequestException("Goi dich vu dang co yeu cau kich hoat cho xu ly");
        }
        ServicePackage servicePackage = orderItem.getServicePackage();

        ServiceRegistration registration = new ServiceRegistration();
        registration.setCustomer(customer);
        registration.setServicePackage(servicePackage);
        registration.setOrderItem(orderItem);
        registration.setRequestId(request.requestId().trim());
        registration.setStatus(RegistrationStatus.PENDING);
        registration = registrationRepository.saveAndFlush(registration);

        logIntegration(registration, "EXTERNAL_REQUEST", "SENT", "Gui yeu cau kich hoat sang MOCK-EWS");

        try {
            ActivationPartnerResponse response = activationPartnerClient.activate(new ActivationPartnerCommand(
                    registration.getRequestId(),
                    registration.getRegistrationCode(),
                    servicePackage.getCode(),
                    customer.getUsername(),
                    request.simulation()
            ));

            registration.setStatus(RegistrationStatus.ACTIVATED);
            registration.setExternalReference(response.externalReference());
            registration.setFailureCode(null);
            registration.setFailureReason(null);
            registration = registrationRepository.save(registration);
            logIntegration(registration, "EXTERNAL_RESPONSE", "ACTIVATED", "MOCK-EWS kich hoat thanh cong");
            auditLogService.log(customer.getUsername(), "ACTIVATE", "SERVICE_REGISTRATION", registration.getId(),
                    "Activated " + registration.getRegistrationCode());
            return toResponse(registration, false);
        } catch (PartnerBusinessException exception) {
            return markFailed(registration, customer, EXTERNAL_BUSINESS_ERROR, exception.getMessage(), "FAILED");
        } catch (PartnerTimeoutException exception) {
            return markFailed(registration, customer, EXTERNAL_TIMEOUT, exception.getMessage(), "TIMEOUT");
        }
    }

    @Transactional(readOnly = true)
    public List<ServiceRegistrationResponse> getMyRegistrations(String username) {
        return registrationRepository.findByCustomerUsernameIgnoreCaseOrderByCreatedAtDesc(username).stream()
                .map(registration -> toResponse(registration, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchasedServiceResponse> getPurchasedServices(String username) {
        return orderItemRepository
                .findByOrderCustomerUsernameIgnoreCaseAndOrderStatusOrderByOrderCreatedAtDesc(username, "COMPLETED")
                .stream()
                .map(orderItem -> {
                    ServiceRegistration latest = registrationRepository
                            .findTopByOrderItemIdOrderByCreatedAtDesc(orderItem.getId())
                            .orElse(null);
                    boolean canActivate = latest == null || latest.getStatus() == RegistrationStatus.FAILED;
                    return new PurchasedServiceResponse(
                            orderItem.getId(),
                            orderItem.getOrder().getId(),
                            orderItem.getOrder().getOrderCode(),
                            orderItem.getServicePackage().getId(),
                            orderItem.getPackageCode(),
                            orderItem.getPackageName(),
                            orderItem.getQuantity(),
                            latest == null ? null : latest.getStatus(),
                            canActivate
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceRegistrationResponse> getAllRegistrations() {
        return registrationRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(registration -> toResponse(registration, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IntegrationLogResponse> getRecentIntegrationLogs() {
        return integrationLogRepository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(log -> new IntegrationLogResponse(
                        log.getId(),
                        log.getRegistration().getId(),
                        log.getRegistration().getRegistrationCode(),
                        log.getRequestId(),
                        log.getTraceId(),
                        log.getEventType(),
                        log.getOutcome(),
                        log.getDetail(),
                        log.getCreatedAt()
                ))
                .toList();
    }

    private ServiceRegistrationResponse markFailed(
            ServiceRegistration registration,
            AppUser customer,
            String errorCode,
            String reason,
            String outcome
    ) {
        registration.setStatus(RegistrationStatus.FAILED);
        registration.setFailureCode(errorCode);
        registration.setFailureReason(reason);
        registration = registrationRepository.save(registration);
        logIntegration(registration, "EXTERNAL_RESPONSE", outcome, reason);
        auditLogService.log(customer.getUsername(), "ACTIVATION_FAILED", "SERVICE_REGISTRATION", registration.getId(),
                errorCode + ": " + reason);
        return toResponse(registration, false);
    }

    private void logIntegration(ServiceRegistration registration, String eventType, String outcome, String detail) {
        IntegrationLog log = new IntegrationLog();
        log.setRegistration(registration);
        log.setRequestId(registration.getRequestId());
        log.setTraceId(MDC.get("traceId"));
        log.setEventType(eventType);
        log.setOutcome(outcome);
        log.setDetail(detail);
        integrationLogRepository.save(log);
    }

    private ServiceRegistrationResponse toResponse(ServiceRegistration registration, boolean replayed) {
        boolean retryable = EXTERNAL_TIMEOUT.equals(registration.getFailureCode());
        String message = switch (registration.getStatus()) {
            case ACTIVATED -> "Kich hoat dich vu thanh cong";
            case FAILED -> registration.getFailureReason();
            case PENDING -> "Yeu cau dang cho xu ly";
        };
        CustomerOrderItem orderItem = registration.getOrderItem();
        return new ServiceRegistrationResponse(
                registration.getId(),
                registration.getRegistrationCode(),
                registration.getRequestId(),
                orderItem == null ? null : orderItem.getId(),
                orderItem == null ? null : orderItem.getOrder().getId(),
                orderItem == null ? null : orderItem.getOrder().getOrderCode(),
                registration.getServicePackage().getId(),
                registration.getServicePackage().getCode(),
                registration.getServicePackage().getName(),
                registration.getStatus(),
                registration.getFailureCode(),
                message,
                registration.getExternalReference(),
                MDC.get("traceId"),
                replayed,
                retryable,
                registration.getCreatedAt(),
                registration.getUpdatedAt()
        );
    }
}
