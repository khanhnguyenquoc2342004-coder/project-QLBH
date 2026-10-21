package com.example.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesSummaryResponse(
        long totalOrders,
        long pendingOrders,
        long completedOrders,
        long totalCustomers,
        BigDecimal revenue,
        List<TopPackage> topPackages
) {
    public record TopPackage(String name, long quantity) { }
}
