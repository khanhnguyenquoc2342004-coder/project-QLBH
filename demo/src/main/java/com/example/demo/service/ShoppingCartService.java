package com.example.demo.service;

import com.example.demo.dto.CartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.ServicePackage;
import com.example.demo.entity.ShoppingCartItem;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.ServicePackageRepository;
import com.example.demo.repository.ShoppingCartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ShoppingCartService {
    private final ShoppingCartItemRepository cartRepository;
    private final AppUserRepository userRepository;
    private final ServicePackageRepository packageRepository;

    public ShoppingCartService(ShoppingCartItemRepository cartRepository, AppUserRepository userRepository, ServicePackageRepository packageRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
    }

    @Transactional(readOnly = true)
    public List<CartItemResponse> getCart(String username) {
        return cartRepository.findByCustomerUsernameIgnoreCaseOrderByIdAsc(username).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<CartItemResponse> addItem(String username, CartItemRequest request) {
        AppUser customer = getCustomer(username);
        ServicePackage servicePackage = getActivePackage(request.packageId());
        ShoppingCartItem item = cartRepository.findByCustomerUsernameIgnoreCaseAndServicePackageId(username, request.packageId())
                .orElseGet(() -> {
                    ShoppingCartItem newItem = new ShoppingCartItem();
                    newItem.setCustomer(customer);
                    newItem.setServicePackage(servicePackage);
                    newItem.setQuantity(0);
                    return newItem;
                });
        int quantity = item.getQuantity() + request.quantity();
        if (quantity > 100) {
            throw new BadRequestException("Số lượng của một gói không được vượt quá 100");
        }
        item.setQuantity(quantity);
        cartRepository.save(item);
        return getCart(username);
    }

    @Transactional
    public List<CartItemResponse> updateItem(String username, Long packageId, CartItemRequest request) {
        if (!packageId.equals(request.packageId())) {
            throw new BadRequestException("Gói dịch vụ không khớp");
        }
        ShoppingCartItem item = cartRepository.findByCustomerUsernameIgnoreCaseAndServicePackageId(username, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói dịch vụ trong giỏ hàng"));
        item.setQuantity(request.quantity());
        cartRepository.save(item);
        return getCart(username);
    }

    @Transactional
    public void removeItem(String username, Long packageId) {
        ShoppingCartItem item = cartRepository.findByCustomerUsernameIgnoreCaseAndServicePackageId(username, packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói dịch vụ trong giỏ hàng"));
        cartRepository.delete(item);
    }

    @Transactional
    public void clearCart(String username) {
        cartRepository.deleteByCustomerUsernameIgnoreCase(username);
    }

    private AppUser getCustomer(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản hiện tại"));
    }

    private ServicePackage getActivePackage(Long packageId) {
        ServicePackage servicePackage = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gói dịch vụ"));
        if (!"ACTIVE".equalsIgnoreCase(servicePackage.getStatus())) {
            throw new BadRequestException("Gói dịch vụ hiện không còn kinh doanh");
        }
        return servicePackage;
    }

    private CartItemResponse toResponse(ShoppingCartItem item) {
        ServicePackage servicePackage = item.getServicePackage();
        return new CartItemResponse(item.getId(), servicePackage.getId(), servicePackage.getCode(), servicePackage.getName(),
                servicePackage.getDescription(), servicePackage.getImageUrl(), servicePackage.getPrice(),
                servicePackage.getDiscountPercent(), item.getQuantity());
    }
}
