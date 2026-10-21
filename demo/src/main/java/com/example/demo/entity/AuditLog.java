package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String actor;
    @Column(nullable = false, length = 80)
    private String action;
    @Column(nullable = false, length = 50)
    private String targetType;
    @Column(length = 50)
    private String targetId;
    @Column(length = 500)
    private String detail;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @PrePersist void initializeTimestamp() { if (createdAt == null) createdAt = Instant.now(); }
}
