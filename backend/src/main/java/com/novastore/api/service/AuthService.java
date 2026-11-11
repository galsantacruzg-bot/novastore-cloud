package com.novastore.api.service;

import com.novastore.api.dto.*;
import com.novastore.api.entity.AuthToken;
import com.novastore.api.entity.UserAccount;
import com.novastore.api.repository.AuthTokenRepository;
import com.novastore.api.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final AuthTokenRepository tokens;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final HashService hashService;
    private final int tokenHours;
    private final boolean verificationRequired;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserAccountRepository users,
                       AuthTokenRepository tokens,
                       PasswordEncoder passwordEncoder,
                       VerificationService verificationService,
                       HashService hashService,
                       @Value("${app.auth.token-hours}") int tokenHours,
                       @Value("${app.auth.email-verification-required:false}") boolean verificationRequired) {
        this.users = users;
        this.tokens = tokens;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.hashService = hashService;
        this.tokenHours = tokenHours;
        this.verificationRequired = verificationRequired;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Ese correo ya está registrado.");
        if (users.existsByUsernameIgnoreCase(username)) throw new IllegalArgumentException("Ese nombre de usuario ya existe.");

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setVerified(!verificationRequired);
        users.save(user);
        if (verificationRequired) verificationService.issue(user);
    }

    @Transactional
    public void verify(VerifyAccountRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada."));
        if (user.isVerified()) return;
        if (!verificationService.verify(user, request.code())) {
            throw new IllegalArgumentException("Código de verificación incorrecto.");
        }
        user.setVerified(true);
        users.save(user);
    }

    @Transactional
    public void resend(ResendVerificationRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada."));
        if (user.isVerified()) throw new IllegalArgumentException("La cuenta ya está verificada.");
        verificationService.issue(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = findByIdentifier(request.identifier())
                .orElseThrow(() -> new IllegalArgumentException("Usuario/correo o contraseña incorrectos."));
        if (!user.isActive()) throw new IllegalArgumentException("La cuenta está desactivada.");
        if (!user.isVerified()) throw new IllegalArgumentException("Debes verificar tu correo antes de iniciar sesión.");
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Usuario/correo o contraseña incorrectos.");
        }

        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        AuthToken token = new AuthToken();
        token.setUser(user);
        token.setTokenHash(hashService.sha256(rawToken));
        token.setExpiresAt(LocalDateTime.now().plusHours(tokenHours));
        tokens.save(token);
        return new AuthResponse(rawToken, user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @Transactional(readOnly = true)
    public Optional<UserAccount> authenticateToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return Optional.empty();
        return tokens.findByTokenHashAndRevokedFalseAndExpiresAtAfter(hashService.sha256(rawToken), LocalDateTime.now())
                .map(AuthToken::getUser)
                .filter(user -> user.isActive() && user.isVerified());
    }

    @Transactional
    public void logout(String rawToken) {
        tokens.findByTokenHashAndRevokedFalseAndExpiresAtAfter(hashService.sha256(rawToken), LocalDateTime.now())
                .ifPresent(token -> { token.setRevoked(true); tokens.save(token); });
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(String username) {
        UserAccount user = users.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.isVerified());
    }

    private Optional<UserAccount> findByIdentifier(String identifier) {
        String value = identifier.trim();
        return value.contains("@") ? users.findByEmailIgnoreCase(value) : users.findByUsernameIgnoreCase(value);
    }
}
