package com.example.demo.repository;

import com.example.demo.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomerUsernameIgnoreCaseOrderByCreatedAtDesc(String username);

}
