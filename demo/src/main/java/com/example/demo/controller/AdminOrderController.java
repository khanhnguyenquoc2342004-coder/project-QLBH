package com.example.demo.controller;

import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.OrderUpdateRequest;
import com.example.demo.dto.SalesSummaryResponse;
import com.example.demo.service.CustomerOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminOrderController {
    private final CustomerOrderService orderService;
    public AdminOrderController(CustomerOrderService orderService) { this.orderService = orderService; }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders(@RequestParam(required = false) String status, @RequestParam(required = false) String keyword) {
        return orderService.getAllOrders(status, keyword);
    }

    @PutMapping("/orders/{id}")
    public OrderResponse updateOrder(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request, Authentication authentication) {
        return orderService.updateOrder(id, request, authentication.getName());
    }

    @GetMapping("/dashboard")
    public SalesSummaryResponse getDashboard() { return orderService.getSalesSummary(); }
}
