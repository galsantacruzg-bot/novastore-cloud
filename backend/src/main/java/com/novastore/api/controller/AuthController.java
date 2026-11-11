package com.novastore.api.controller;

import com.novastore.api.dto.*;
import com.novastore.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final boolean verificationRequired;

    public AuthController(AuthService authService,
            @Value("${app.auth.email-verification-required:false}") boolean verificationRequired) {
        this.authService = authService;
        this.verificationRequired = verificationRequired;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Map.of("message", verificationRequired
                ? "Cuenta creada. Revisa tu correo e ingresa el código de 6 dígitos."
                : "Cuenta creada. En este entorno académico puedes iniciar sesión sin verificación de correo.",
                "verificationRequired", verificationRequired);
    }

    @PostMapping("/verify")
    public Map<String, String> verify(@Valid @RequestBody VerifyAccountRequest request) {
        authService.verify(request);
        return Map.of("message", "Cuenta verificada correctamente.");
    }

    @PostMapping("/resend")
    public Map<String, String> resend(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resend(request);
        return Map.of("message", "Se envió un nuevo código de verificación.");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        // Todo usuario válido puede iniciar sesión desde cualquier dispositivo.
        // La autorización de operaciones administrativas se valida en cada endpoint.
        return authService.login(request);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader(value = "Authorization", required = false) String header) {
        if (header != null && header.startsWith("Bearer ")) authService.logout(header.substring(7));
        return Map.of("message", "Sesión cerrada.");
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return authService.profile(authentication.getName());
    }
}
