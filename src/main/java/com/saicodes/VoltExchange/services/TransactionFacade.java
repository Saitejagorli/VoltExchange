package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.dto.TransactionRequest;
import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.events.TransactionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionFacade {

    private final TransactionService transactionService;
    private final CacheManager cacheManager;
    private final ApplicationEventPublisher eventPublisher;

    public Transaction transfer(User sender , TransactionRequest transactionRequest){
        Transaction transaction = transactionService.transfer(sender, transactionRequest);
        eventPublisher.publishEvent(new TransactionEvent(this,transaction));
        if(cacheManager.getCache("wallets") != null){
            cacheManager.getCache("wallets").evict(sender.getId());
            cacheManager.getCache("wallets").evict(transaction.getReceiverWallet().getUser().getId());
        }
        return transaction;
    }
}
