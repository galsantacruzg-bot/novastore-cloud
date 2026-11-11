package com.novastore.api.repository;

import com.novastore.api.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findFirstByUserIdAndUsedFalseOrderByCreatedAtDesc(Long userId);
}
