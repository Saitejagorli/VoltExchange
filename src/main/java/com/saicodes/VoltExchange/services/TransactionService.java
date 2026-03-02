package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.dto.TransactionRequest;
import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.entities.Wallet;
import com.saicodes.VoltExchange.enums.TransactionStatus;
import com.saicodes.VoltExchange.enums.TransactionType;
import com.saicodes.VoltExchange.exceptions.WalletException;
import com.saicodes.VoltExchange.repositories.TransactionRepository;
import com.saicodes.VoltExchange.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    CannotAcquireLockException.class
            },
            maxAttempts = 15,
            backoff = @Backoff(delay = 50, multiplier = 1.5, maxDelay = 1000)
    )
    @Transactional
    public Transaction transfer(User sender, TransactionRequest transactionRequest) {

        Wallet senderWallet = walletRepository.findByUser_Id(sender.getId()).orElseThrow(() -> new WalletException("Sender wallet not found", HttpStatus.NOT_FOUND));

        User receiver = userService.getUserByEmail(transactionRequest.receiverEmail());
        Wallet receiverWallet = walletRepository.findByUser_Id(receiver.getId()).orElseThrow(() -> new WalletException("Receiver wallet not found", HttpStatus.NOT_FOUND));

        // finding the smallest id to avoid deadlock
        boolean isSenderFirst = senderWallet.getId().compareTo(receiverWallet.getId()) < 0;
        Wallet firstLock = isSenderFirst ? senderWallet : receiverWallet;
        Wallet secondLock = isSenderFirst ? receiverWallet : senderWallet;

        if (senderWallet.getId().equals(receiverWallet.getId())) {
            throw new WalletException("Cannot transfer to your own wallet", HttpStatus.BAD_REQUEST);
        }

        if (senderWallet.getBalance().compareTo(transactionRequest.amount()) < 0) {
            throw new WalletException("Insufficient Balance", HttpStatus.BAD_REQUEST);
        }
        Transaction transaction = transactionRepository.save(Transaction.builder()
                .senderWallet(senderWallet)
                .receiverWallet(receiverWallet)
                .amount(transactionRequest.amount())
                .type(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .build());

        senderWallet.setBalance(senderWallet.getBalance().subtract(transactionRequest.amount()));
        receiverWallet.setBalance(receiverWallet.getBalance().add(transactionRequest.amount()));

        walletRepository.saveAndFlush(firstLock);
        walletRepository.saveAndFlush(secondLock);

        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.saveAndFlush(transaction);

    }

    @Recover
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction recoverFailedTransfer(Exception e, User sender, TransactionRequest transactionRequest) {
        Wallet senderWallet = walletRepository.findByUser_Id(sender.getId()).orElse(null);
        User receiver = userService.getUserByEmail(transactionRequest.receiverEmail());
        Wallet receiverWallet = walletRepository.findByUser_Id(receiver.getId()).orElse(null);

        String failureReason = "UNEXPECTED SYSTEM ERROR";
        if( e instanceof ObjectOptimisticLockingFailureException ) {
            failureReason = ((ObjectOptimisticLockingFailureException) e).getCause().getMessage();
        }
        else if( e instanceof CannotAcquireLockException ) {
            failureReason = "Cannot acquire lock";
        }
        else if( e instanceof WalletException ) {
            failureReason = e.getMessage();
        }

        Transaction failedTransaction = Transaction.builder()
                                        .senderWallet(senderWallet)
                                        .receiverWallet(receiverWallet)
                                        .amount(transactionRequest.amount())
                                        .type(TransactionType.TRANSFER)
                                        .status(TransactionStatus.FAILED)
                                        .failureReason(failureReason)
                                        .build();
        logger.error("Transaction failed for sender "+ sender.getEmail() +" reason "+e.getMessage());
        return transactionRepository.saveAndFlush(failedTransaction);
    }


}
