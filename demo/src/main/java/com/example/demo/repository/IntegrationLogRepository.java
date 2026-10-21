package com.example.demo.repository;

import com.example.demo.entity.IntegrationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, Long> {

    List<IntegrationLog> findTop100ByOrderByCreatedAtDesc();
}
