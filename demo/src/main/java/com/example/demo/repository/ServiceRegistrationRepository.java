package com.example.demo.repository;

import com.example.demo.entity.ServiceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceRegistrationRepository extends JpaRepository<ServiceRegistration, Long> {

    Optional<ServiceRegistration> findByRequestId(String requestId);

    Optional<ServiceRegistration> findTopByOrderItemIdOrderByCreatedAtDesc(Long orderItemId);

    List<ServiceRegistration> findByCustomerUsernameIgnoreCaseOrderByCreatedAtDesc(String username);

    List<ServiceRegistration> findAllByOrderByCreatedAtDesc();
}
