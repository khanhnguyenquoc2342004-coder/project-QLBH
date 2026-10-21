package com.example.demo.repository;

import com.example.demo.entity.CustomerOrderItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderItemRepository extends JpaRepository<CustomerOrderItem, Long> {

    @EntityGraph(attributePaths = {"order", "servicePackage"})
    List<CustomerOrderItem> findByOrderCustomerUsernameIgnoreCaseAndOrderStatusOrderByOrderCreatedAtDesc(
            String username,
            String status
    );
}
