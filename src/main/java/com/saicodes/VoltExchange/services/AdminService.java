package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.dto.TransactionResponse;
import com.saicodes.VoltExchange.entities.Transaction;
import com.saicodes.VoltExchange.events.TransactionEvent;
import com.saicodes.VoltExchange.repositories.TransactionRepository;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AdminService {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();;
    private final TransactionRepository transactionRepository;

    public AdminService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("stream started"));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        return emitter;
    }

    @EventListener
    public void onTransactionEvent(TransactionEvent transactionEvent) throws IOException {
        sendToEmitters(transactionEvent.getTransaction());

    }

    @Async
    public void sendToEmitters(Transaction transaction) throws IOException {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("transaction")
                        .data(TransactionResponse.from(transaction))
                        .build());
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public Page<Transaction> findTransactions(int page, int size, boolean asc) {
        Sort sort = asc  ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size,sort);
        return transactionRepository.findAll(pageable);
    }
}
