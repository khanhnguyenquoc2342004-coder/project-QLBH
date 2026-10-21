package com.example.demo.controller;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.service.CustomerOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(authentication.getName(), request));
    }

    @GetMapping("/my")
    public List<OrderResponse> getMyOrders(Authentication authentication) {
        return orderService.getOrdersForCustomer(authentication.getName());
    }

    @PostMapping("/my/{id}/cancel")
    public OrderResponse cancelMyOrder(@PathVariable Long id, Authentication authentication) {
        return orderService.cancelOrder(authentication.getName(), id);
    }

    @PostMapping("/my/{id}/refund-request")
    public OrderResponse requestRefund(@PathVariable Long id, Authentication authentication) {
        return orderService.requestRefund(authentication.getName(), id);
    }
}
