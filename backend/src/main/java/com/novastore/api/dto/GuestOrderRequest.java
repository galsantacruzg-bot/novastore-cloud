package com.novastore.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GuestOrderRequest(
        @NotBlank @Size(min = 3, max = 120) String name,
        @NotBlank @Email @Size(max = 180) String email,
        @NotBlank @Size(min = 7, max = 30) String phone,
        @NotBlank @Size(min = 8, max = 700) String shippingAddress,
        @Valid @NotEmpty @Size(max = 30) List<OrderItemRequest> items
) {}
