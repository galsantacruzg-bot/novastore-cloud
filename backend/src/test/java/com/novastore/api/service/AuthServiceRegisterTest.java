package com.novastore.api.service;

import com.novastore.api.dto.RegisterRequest;
import com.novastore.api.entity.UserAccount;
import com.novastore.api.repository.AuthTokenRepository;
import com.novastore.api.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceRegisterTest {
    @Test
    void registroAcademicoNoDependeDeSMTPYNoAsignaRolAdmin() {
        var users = mock(UserAccountRepository.class);
        var tokens = mock(AuthTokenRepository.class);
        var passwordEncoder = mock(PasswordEncoder.class);
        var verification = mock(VerificationService.class);
        var hashes = mock(HashService.class);
        when(passwordEncoder.encode(anyString())).thenReturn("hash-bcrypt-demo");
        var service = new AuthService(users, tokens, passwordEncoder, verification, hashes, 12, false);

        service.register(new RegisterRequest("clienteDemo", "cliente@example.test", "claveSeguraDemo123"));

        var captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(captor.capture());
        assertEquals("USER", captor.getValue().getRole());
        assertTrue(captor.getValue().isVerified()); // Solo habilitación académica, email no verificado realmente
        assertEquals("hash-bcrypt-demo", captor.getValue().getPasswordHash());
        verifyNoInteractions(verification);
    }

    @Test
    void registroVerificadoRequiereEmisionDeCodigo() {
        var users = mock(UserAccountRepository.class);
        var verification = mock(VerificationService.class);
        var passwordEncoder = mock(PasswordEncoder.class);
        var service = new AuthService(users, mock(AuthTokenRepository.class), passwordEncoder,
                verification, mock(HashService.class), 12, true);
        service.register(new RegisterRequest("clienteDemo", "cliente@example.test", "claveSeguraDemo123"));
        var captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(users).save(captor.capture());
        assertFalse(captor.getValue().isVerified());
        verify(verification).issue(captor.getValue());
    }
}
