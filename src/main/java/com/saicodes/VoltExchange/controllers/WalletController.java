package com.saicodes.VoltExchange.controllers;


import com.saicodes.VoltExchange.common.ApiResponse;
import com.saicodes.VoltExchange.dto.DepositRequest;
import com.saicodes.VoltExchange.dto.TransactionRequest;
import com.saicodes.VoltExchange.dto.TransactionResponse;
import com.saicodes.VoltExchange.dto.WalletResponse;
import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.entities.Wallet;
import com.saicodes.VoltExchange.enums.TransactionStatus;
import com.saicodes.VoltExchange.exceptions.WalletException;
import com.saicodes.VoltExchange.services.TransactionService;
import com.saicodes.VoltExchange.services.WalletService;
import com.saicodes.VoltExchange.util.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/wallets")
public class WalletController {
    private final SecurityUtils securityUtils;
    private final WalletService walletService;
    private final TransactionService transactionService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet() {
        User user = securityUtils.getCurrentUser();
        Wallet wallet = walletService.getWallet(user).orElseThrow(() -> new WalletException("wallet not found", HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().body(ApiResponse.success("Wallet fetched successfully", WalletResponse.from(wallet)));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<WalletResponse>> deposit(@RequestBody @Valid DepositRequest depositRequest){
        User user = securityUtils.getCurrentUser();
        Wallet wallet = walletService.depositMoney(user, depositRequest.amount());
        return ResponseEntity.ok().body(ApiResponse.success("Amount deposited Successfully", WalletResponse.from(wallet)));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transferAmount(@RequestBody @Valid TransactionRequest transactionRequest){
        User user = securityUtils.getCurrentUser();
        Transaction transaction = transactionService.transfer(user, transactionRequest);
        if(transaction.getStatus() == TransactionStatus.FAILED){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.success("Transaction failed", TransactionResponse.from(transaction)));
        }
        return ResponseEntity.ok().body(ApiResponse.success("Amount transferred Successfully",TransactionResponse.from(transaction)));
    }
}
