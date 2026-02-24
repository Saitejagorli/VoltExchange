package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.entities.Wallet;
import com.saicodes.VoltExchange.exceptions.WalletException;
import com.saicodes.VoltExchange.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@AllArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final UserService userService;

    public void createWallet(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .build();
        walletRepository.save(wallet);
    }

    @Cacheable(value = "wallets", key = "#user.id")
    public Optional<Wallet> getWallet(User user) {
        return walletRepository.findByUser_Id(user.getId());
    }

    @Transactional
    public Wallet depositMoney(User user, BigDecimal money) {
        Wallet wallet = walletRepository.findByUser_Id(user.getId()).orElseThrow(() -> new WalletException("wallet not found", HttpStatus.NOT_FOUND));
        wallet.setBalance(wallet.getBalance().add(money));
        return walletRepository.save(wallet);
    }
}
