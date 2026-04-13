package com.saicodes.VoltExchange;

import com.saicodes.VoltExchange.dto.TransactionRequest;
import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.entities.Wallet;
import com.saicodes.VoltExchange.repositories.TransactionRepository;
import com.saicodes.VoltExchange.repositories.WalletRepository;
import com.saicodes.VoltExchange.services.TransactionService;
import com.saicodes.VoltExchange.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class VoltExchangeApplicationTests {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UserService userService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

//    @Test
//    public void testConcurrentTransfer() throws InterruptedException {
//        int threadCount = 10;
//        BigDecimal transferAmount = new BigDecimal("1");
//
//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
//
//        CountDownLatch latch = new CountDownLatch(threadCount);
//        User sender = userService.getUserByEmail("a@g.com");
//        User receiver = userService.getUserByEmail("b@g.com");
//
//        for (int i = 0; i < threadCount; i++) {
//            executor.submit(() -> {
//                try {
//                    transactionService.transfer(sender, new TransactionRequest("b@g.com", transferAmount));
//                } catch (Exception e) {
//                    System.out.println("Transfer failed" + e.getMessage());
//                } finally {
//                    latch.countDown();
//                }
//            });
//
//        }
//        latch.await();
//        executor.shutdown();
//
//        List<Transaction> all = transactionRepository.findAll();
//        System.out.println("Total transactions recorded: " + all.size());
//        all.forEach(t ->
//                System.out.println("Status: " + t.getStatus() + " | Amount: " + t.getAmount()
//        ));
//
//
//        Wallet senderWallet = walletRepository.findByUser_Id(sender.getId()).orElse(null);
//        Wallet receiverWallet = walletRepository.findByUser_Id(receiver.getId()).get();
//
//        System.out.println("Sender balance: " + senderWallet.getBalance());
//        System.out.println("Receiver balance: " + receiverWallet.getBalance());
//        System.out.println("Total: " + senderWallet.getBalance().add(receiverWallet.getBalance()));
//
//        assertEquals(new BigDecimal("90.0000"), senderWallet.getBalance());
//
//
//    }

}
