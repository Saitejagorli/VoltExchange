package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.dto.TransactionRequest;
import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.entities.User;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Service;

@Service
public class TransactionFacade {

    private final TransactionService transactionService;
    private final RedisCacheManager cacheManager;

    public TransactionFacade(TransactionService transactionService, RedisCacheManager cacheManager) {
        this.transactionService = transactionService;
        this.cacheManager = cacheManager;
    }

    public Transaction transfer(User sender , TransactionRequest transactionRequest){
        Transaction transaction = transactionService.transfer(sender, transactionRequest);
        cacheManager.getCache("wallets").evict(sender.getId());
        cacheManager.getCache("wallets").evict(transaction.getReceiverWallet().getUser().getId());
        return transaction;
    }
}
