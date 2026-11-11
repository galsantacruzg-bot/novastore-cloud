package com.novastore.api.service;

import com.novastore.api.entity.UserAccount;
import com.novastore.api.entity.VerificationCode;
import com.novastore.api.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class VerificationService {
    private final VerificationCodeRepository repository;
    private final HashService hashService;
    private final MailService mailService;
    private final SecureRandom random = new SecureRandom();
    private final int expirationMinutes;
    private final int maxAttempts;

    public VerificationService(VerificationCodeRepository repository,
                               HashService hashService,
                               MailService mailService,
                               @Value("${app.auth.verification-minutes}") int expirationMinutes,
                               @Value("${app.auth.max-verification-attempts}") int maxAttempts) {
        this.repository = repository;
        this.hashService = hashService;
        this.mailService = mailService;
        this.expirationMinutes = expirationMinutes;
        this.maxAttempts = maxAttempts;
    }

    @Transactional
    public void issue(UserAccount user) {
        repository.findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId()).ifPresent(previous -> {
            if (previous.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                throw new IllegalArgumentException("Espera al menos 60 segundos antes de solicitar otro código.");
            }
            previous.setUsed(true);
            repository.save(previous);
        });

        String code = String.format("%06d", random.nextInt(1_000_000));
        VerificationCode verification = new VerificationCode();
        verification.setUser(user);
        verification.setCodeHash(hashService.sha256(code));
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(expirationMinutes));
        repository.save(verification);

        mailService.sendVerificationCode(user.getEmail(), user.getUsername(), code);
        mailService.notifyAdminRegistration(user.getUsername(), user.getEmail());
    }

    @Transactional
    public boolean verify(UserAccount user, String rawCode) {
        VerificationCode verification = repository
                .findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("No existe un código de verificación activo."));

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            verification.setUsed(true);
            repository.save(verification);
            throw new IllegalArgumentException("El código expiró. Solicita uno nuevo.");
        }
        if (verification.getAttempts() >= maxAttempts) {
            verification.setUsed(true);
            repository.save(verification);
            throw new IllegalArgumentException("Se superó el número de intentos. Solicita un nuevo código.");
        }

        verification.setAttempts(verification.getAttempts() + 1);
        boolean matches = verification.getCodeHash().equals(hashService.sha256(rawCode));
        if (matches) verification.setUsed(true);
        repository.save(verification);
        return matches;
    }
}
