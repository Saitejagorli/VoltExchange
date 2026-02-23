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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final WalletService walletService;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional
    public Transaction transfer(User sender, TransactionRequest transactionRequest) {

        Wallet senderWallet = walletRepository.findByUser_Id(sender.getId()).orElseThrow(() -> new WalletException("Sender wallet not found", HttpStatus.NOT_FOUND));


        User receiver = userService.getUserByEmail(transactionRequest.receiverEmail());
        Wallet receiverWallet = walletRepository.findByUser_Id(receiver.getId()).orElseThrow(() -> new WalletException("Receiver wallet not found", HttpStatus.NOT_FOUND));

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

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        transaction.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(transaction);

    }


}
