package com.saicodes.VoltExchange.repositories;

import com.saicodes.VoltExchange.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
        public Optional<RefreshToken> findByToken(String token);
}
