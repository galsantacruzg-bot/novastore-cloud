package com.novastore.api.service;

import com.novastore.api.dto.LoginRequest;
import com.novastore.api.entity.AuthToken;
import com.novastore.api.entity.UserAccount;
import com.novastore.api.repository.AuthTokenRepository;
import com.novastore.api.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceLoginTest {

    @Test
    void adminPuedeIniciarSesionDesdeEnlacePublicoSinHabilitarEndpointsAdministrativos() {
        var users = mock(UserAccountRepository.class);
        var tokens = mock(AuthTokenRepository.class);
        var passwordEncoder = mock(PasswordEncoder.class);
        var verification = mock(VerificationService.class);
        var hashes = mock(HashService.class);

        var admin = new UserAccount();
        admin.setUsername("administrador");
        admin.setEmail("admin@example.test");
        admin.setRole("ADMIN");
        admin.setVerified(true);
        admin.setActive(true);
        admin.setPasswordHash("hash-de-prueba");

        when(users.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("clave-de-prueba", "hash-de-prueba")).thenReturn(true);
        when(hashes.sha256(anyString())).thenReturn("token-hash-de-prueba");

        var service = new AuthService(users, tokens, passwordEncoder, verification, hashes, 8, false);
        var result = service.login(new LoginRequest("admin@example.test", "clave-de-prueba"));

        assertEquals("ADMIN", result.role());
        assertNotNull(result.token());
        verify(tokens).save(any(AuthToken.class));
    }

    @Test
    void contraseñaIncorrectaNoGeneraToken() {
        var users = mock(UserAccountRepository.class);
        var tokens = mock(AuthTokenRepository.class);
        var passwordEncoder = mock(PasswordEncoder.class);
        var admin = new UserAccount();
        admin.setEmail("admin@example.test");
        admin.setUsername("admin");
        admin.setPasswordHash("hash");
        admin.setVerified(true);
        admin.setActive(true);
        when(users.findByEmailIgnoreCase("admin@example.test")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("incorrecta", "hash")).thenReturn(false);
        var service = new AuthService(users, tokens, passwordEncoder, mock(VerificationService.class), mock(HashService.class), 8, false);
        assertThrows(IllegalArgumentException.class,
                () -> service.login(new LoginRequest("admin@example.test", "incorrecta")));
        verifyNoInteractions(tokens);
    }
}
