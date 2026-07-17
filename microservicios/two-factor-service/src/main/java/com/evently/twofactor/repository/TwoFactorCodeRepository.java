package com.evently.twofactor.repository;

import com.evently.twofactor.model.TwoFactorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TwoFactorCodeRepository extends JpaRepository<TwoFactorCode, Long> {
    Optional<TwoFactorCode> findTopByEmailOrderByExpiryTimeDesc(String email);
    void deleteByEmail(String email);
}
