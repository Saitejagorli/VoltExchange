package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.entities.Wallet;
import com.saicodes.VoltExchange.repositories.WalletRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    public void createWallet(User user){
        Wallet wallet = Wallet.builder()
                .user(user)
                .build();
        walletRepository.save(wallet);
    }
}
