package com.saicodes.VoltExchange.controllers;

import com.saicodes.VoltExchange.common.ApiResponse;
import com.saicodes.VoltExchange.dto.LoginRequest;
import com.saicodes.VoltExchange.dto.LoginResponse;
import com.saicodes.VoltExchange.dto.RegistrationRequest;
import com.saicodes.VoltExchange.services.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponse>> registerUser(@Valid @RequestBody RegistrationRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authenticationService.register(request, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("User registered successfully", loginResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> loginUser(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authenticationService.login(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User logged in successfully", loginResponse));
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authenticationService.refreshToken(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("User refreshed successfully", loginResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logout(request, response);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Logged out successfully", null));
    }
}
