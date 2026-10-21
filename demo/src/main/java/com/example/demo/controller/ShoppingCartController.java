package com.example.demo.controller;

import com.example.demo.dto.CartItemRequest;
import com.example.demo.dto.CartItemResponse;
import com.example.demo.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {
    private final ShoppingCartService cartService;
    public ShoppingCartController(ShoppingCartService cartService) { this.cartService = cartService; }

    @GetMapping
    public List<CartItemResponse> getCart(Authentication authentication) { return cartService.getCart(authentication.getName()); }

    @PostMapping
    public ResponseEntity<List<CartItemResponse>> addItem(@Valid @RequestBody CartItemRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(authentication.getName(), request));
    }

    @PutMapping("/{packageId}")
    public List<CartItemResponse> updateItem(@PathVariable Long packageId, @Valid @RequestBody CartItemRequest request, Authentication authentication) {
        return cartService.updateItem(authentication.getName(), packageId, request);
    }

    @DeleteMapping("/{packageId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long packageId, Authentication authentication) {
        cartService.removeItem(authentication.getName(), packageId);
        return ResponseEntity.noContent().build();
    }
}
