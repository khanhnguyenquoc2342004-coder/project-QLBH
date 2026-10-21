package com.example.demo.repository;

import com.example.demo.entity.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {
    List<ShoppingCartItem> findByCustomerUsernameIgnoreCaseOrderByIdAsc(String username);
    Optional<ShoppingCartItem> findByCustomerUsernameIgnoreCaseAndServicePackageId(String username, Long packageId);
    void deleteByCustomerUsernameIgnoreCase(String username);
}
