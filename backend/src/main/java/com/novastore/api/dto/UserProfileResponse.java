package com.novastore.api.dto;

public record UserProfileResponse(Long id, String username, String email, String role, boolean verified) {}
