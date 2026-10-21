package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.OrderItemRequest;
import com.example.demo.dto.OrderItemResponse;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.OrderUpdateRequest;
import com.example.demo.dto.SalesSummaryResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.CustomerOrder;
import com.example.demo.entity.CustomerOrderItem;
import com.example.demo.entity.ServicePackage;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.CustomerOrderRepository;
import com.example.demo.repository.ServicePackageRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CustomerOrderService {
    private final CustomerOrderRepository orderRepository;
    private final AppUserRepository userRepository;
    private final ServicePackageRepository packageRepository;
    private final ShoppingCartService cartService;
    private final AuditLogService auditLogService;

    public CustomerOrderService(CustomerOrderRepository orderRepository, AppUserRepository userRepository,
                                ServicePackageRepository packageRepository, ShoppingCartService cartService,
                                AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
        this.cartService = cartService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        AppUser customer = getCustomer(username);
        if (!hasText(customer.getPhone()) || !hasText(customer.getAddress())) {
            throw new BadRequestException("Hãy cập nhật số điện thoại và địa chỉ trước khi đặt hàng");
        }

        CustomerOrder order = new CustomerOrder();
        order.setCustomer(customer);
        order.setCustomerName(customer.getFullName());
        order.setCustomerEmail(customer.getEmail());
        order.setCustomerPhone(customer.getPhone());
        order.setCustomerAddress(customer.getAddress());
        order.setPaymentMethod(hasText(request.paymentMethod()) ? request.paymentMethod() : "BANK_TRANSFER");
        order.setNote(hasText(request.note()) ? request.note().trim() : null);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : consolidateItems(request.items()).entrySet()) {
            ServicePackage servicePackage = getActivePackage(entry.getKey());
            BigDecimal salePrice = discountedPrice(servicePackage);
            CustomerOrderItem item = new CustomerOrderItem();
            item.setServicePackage(servicePackage);
            item.setPackageCode(servicePackage.getCode());
            item.setPackageName(servicePackage.getName());
            item.setUnitPrice(salePrice);
            item.setDiscountPercent(servicePackage.getDiscountPercent());
            item.setQuantity(entry.getValue());
            item.setLineTotal(salePrice.multiply(BigDecimal.valueOf(entry.getValue())));
            order.addItem(item);
            totalAmount = totalAmount.add(item.getLineTotal());
        }
        order.setTotalAmount(totalAmount);

        CustomerOrder saved = orderRepository.save(order);
        saved.setOrderCode("DH-%06d".formatted(saved.getId()));
        cartService.clearCart(username);
        auditLogService.log(username, "CREATE", "ORDER", saved.getId(), "Tạo đơn " + saved.getOrderCode());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersForCustomer(String username) {
        return orderRepository.findByCustomerUsernameIgnoreCaseOrderByCreatedAtDesc(username).stream().map(this::toResponse).toList();
    }

    @Transactional
    public OrderResponse cancelOrder(String username, Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getUsername().equalsIgnoreCase(username)) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BadRequestException("Chỉ có thể hủy đơn đang chờ xác nhận");
        }
        order.setStatus("CANCELLED");
        CustomerOrder saved = orderRepository.save(order);
        auditLogService.log(username, "CANCEL", "ORDER", saved.getId(), "Khách hàng hủy đơn " + saved.getOrderCode());
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse requestRefund(String username, Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (!order.getCustomer().getUsername().equalsIgnoreCase(username)) {
            throw new ResourceNotFoundException("Không tìm thấy đơn hàng");
        }
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BadRequestException("Chỉ có thể yêu cầu hoàn tiền cho đơn đã hoàn tất");
        }
        order.setStatus("REFUND_REQUESTED");
        CustomerOrder saved = orderRepository.save(order);
        auditLogService.log(username, "REQUEST_REFUND", "ORDER", saved.getId(), "Yêu cầu hoàn tiền " + saved.getOrderCode());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders(String status, String keyword) {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .filter(order -> !hasText(status) || order.getStatus().equalsIgnoreCase(status))
                .filter(order -> !hasText(keyword) || contains(order, keyword.trim()))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse updateOrder(Long id, OrderUpdateRequest request, String actor) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        String nextStatus = request.status().trim().toUpperCase();
        if (!nextStatus.equals(order.getStatus()) && !allowedNextStatuses(order.getStatus()).contains(nextStatus)) {
            throw new BadRequestException("Không thể chuyển đơn từ " + order.getStatus() + " sang " + nextStatus);
        }
        order.setStatus(nextStatus);
        if (hasText(request.paymentStatus())) order.setPaymentStatus(request.paymentStatus().trim().toUpperCase());
        if ("REFUNDED".equals(nextStatus)) order.setPaymentStatus("REFUNDED");
        CustomerOrder saved = orderRepository.save(order);
        auditLogService.log(actor, "UPDATE", "ORDER", saved.getId(), "Cập nhật " + saved.getOrderCode() + " thành " + saved.getStatus());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SalesSummaryResponse getSalesSummary() {
        List<CustomerOrder> orders = orderRepository.findAll();
        BigDecimal revenue = orders.stream().filter(order -> "COMPLETED".equals(order.getStatus()))
                .map(CustomerOrder::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Long> quantities = new LinkedHashMap<>();
        orders.stream().filter(order -> !"CANCELLED".equals(order.getStatus())).forEach(order ->
                order.getItems().forEach(item -> quantities.merge(item.getPackageName(), (long) item.getQuantity(), Long::sum))
        );
        List<SalesSummaryResponse.TopPackage> topPackages = quantities.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(5)
                .map(entry -> new SalesSummaryResponse.TopPackage(entry.getKey(), entry.getValue())).toList();
        return new SalesSummaryResponse(
                orders.size(),
                orders.stream().filter(order -> "PENDING".equals(order.getStatus())).count(),
                orders.stream().filter(order -> "COMPLETED".equals(order.getStatus())).count(),
                userRepository.countByRoleAndEnabledTrue(com.example.demo.entity.UserRole.CUSTOMER),
                revenue,
                topPackages
        );
    }

    private Map<Long, Integer> consolidateItems(List<OrderItemRequest> items) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            int quantity = quantities.getOrDefault(item.packageId(), 0) + item.quantity();
            if (quantity > 100) throw new BadRequestException("Số lượng của một gói không được vượt quá 100");
            quantities.put(item.packageId(), quantity);
        }
        return quantities;
    }

    private AppUser getCustomer(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản đặt hàng"));
    }

    private ServicePackage getActivePackage(Long id) {
        ServicePackage servicePackage = packageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói dịch vụ có id: " + id));
        if (!"ACTIVE".equalsIgnoreCase(servicePackage.getStatus())) {
            throw new BadRequestException("Gói dịch vụ " + servicePackage.getName() + " hiện không còn kinh doanh");
        }
        return servicePackage;
    }

    private BigDecimal discountedPrice(ServicePackage servicePackage) {
        return servicePackage.getPrice()
                .multiply(BigDecimal.valueOf(100L - servicePackage.getDiscountPercent()))
                .movePointLeft(2);
    }

    private Set<String> allowedNextStatuses(String status) {
        return switch (status) {
            case "PENDING" -> Set.of("CONFIRMED", "CANCELLED");
            case "CONFIRMED" -> Set.of("COMPLETED", "CANCELLED");
            case "COMPLETED" -> Set.of("REFUND_REQUESTED");
            case "REFUND_REQUESTED" -> Set.of("REFUNDED", "COMPLETED");
            default -> Set.of();
        };
    }

    private boolean contains(CustomerOrder order, String keyword) {
        String value = keyword.toLowerCase();
        return (order.getOrderCode() != null && order.getOrderCode().toLowerCase().contains(value))
                || (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(value))
                || (order.getCustomerPhone() != null && order.getCustomerPhone().contains(value));
    }

    private OrderResponse toResponse(CustomerOrder order) {
        List<OrderItemResponse> items = order.getItems().stream().map(item -> new OrderItemResponse(
                item.getServicePackage().getId(), item.getPackageCode(), item.getPackageName(), item.getUnitPrice(),
                item.getDiscountPercent(), item.getLineTotal(), item.getQuantity()
        )).toList();
        return new OrderResponse(order.getId(), order.getOrderCode(), order.getStatus(), order.getPaymentMethod(),
                order.getPaymentStatus(), order.getTotalAmount(), order.getCreatedAt(), order.getCustomerName(),
                order.getCustomerEmail(), order.getCustomerPhone(), order.getCustomerAddress(), order.getNote(), items);
    }

    private boolean hasText(String value) { return value != null && !value.isBlank(); }
}
