package com.novastore.api.controller;

import com.novastore.api.dto.CreateOrderRequest;
import com.novastore.api.dto.GuestOrderRequest;
import com.novastore.api.entity.CustomerOrder;
import com.novastore.api.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<CustomerOrder> findAll() { return service.findAll(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerOrder create(@Valid @RequestBody CreateOrderRequest request) { return service.create(request); }

    @PostMapping("/guest")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrder guestCheckout(@Valid @RequestBody GuestOrderRequest request) {
        return service.createGuest(request);
    }
}
