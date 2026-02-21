package com.saicodes.VoltExchange.repositories;

import com.saicodes.VoltExchange.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

}
