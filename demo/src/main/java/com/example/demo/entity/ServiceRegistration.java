package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "service_registrations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_registration_request_id", columnNames = "request_id"),
                @UniqueConstraint(name = "uk_registration_code", columnNames = "registration_code")
        }
)
@Getter
@Setter
public class ServiceRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_code", nullable = false, length = 40)
    private String registrationCode;

    @Column(name = "request_id", nullable = false, length = 80)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private AppUser customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_id", nullable = false)
    private ServicePackage servicePackage;

    /** The completed order item that entitles this customer to activation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private CustomerOrderItem orderItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RegistrationStatus status = RegistrationStatus.PENDING;

    @Column(length = 60)
    private String failureCode;

    @Column(length = 500)
    private String failureReason;

    @Column(length = 100)
    private String externalReference;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void initialize() {
        Instant now = Instant.now();
        if (registrationCode == null) {
            registrationCode = "DK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
