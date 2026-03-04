package com.saicodes.VoltExchange.controllers;

import com.saicodes.VoltExchange.common.ApiResponse;
import com.saicodes.VoltExchange.dto.PageResponse;
import com.saicodes.VoltExchange.dto.TransactionResponse;
import com.saicodes.VoltExchange.services.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getTransactions(@RequestParam int page , @RequestParam int size, @RequestParam boolean asc) {
        System.out.println("getTransactions");
        PageResponse<TransactionResponse> pageResponse = PageResponse.from(adminService.findTransactions(page, size, asc).map(TransactionResponse::from));
        return ResponseEntity.ok().body(ApiResponse.success("Data fetched successfully", pageResponse));
    }

    @GetMapping("/transactions/stream")
    public SseEmitter streamTransactions() {
        return adminService.subscribe();
    }
}
