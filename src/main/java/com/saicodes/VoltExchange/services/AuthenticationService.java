package com.saicodes.VoltExchange.services;

import com.saicodes.VoltExchange.dto.LoginRequest;
import com.saicodes.VoltExchange.dto.LoginResponse;
import com.saicodes.VoltExchange.dto.RegistrationRequest;
import com.saicodes.VoltExchange.entities.RefreshToken;
import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.enums.Role;
import com.saicodes.VoltExchange.exceptions.RefreshTokenException;
import com.saicodes.VoltExchange.repositories.RefreshTokenRepository;
import com.saicodes.VoltExchange.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final WalletService walletService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${refreshtoken.expiration.seconds}")
    private Long refreshTokenExpiration;

    public LoginResponse register(RegistrationRequest request, HttpServletResponse response) {
        User user = userService.saveUser(request);
        if(user.getRole() == Role.USER){
            walletService.createWallet(user);
        }
        return issueTokens(user, response);
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userService.getUserByEmail(request.email());

        return issueTokens(user, response);
    }

    public LoginResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String token = extractRefreshTokenFromCookie(request);

        RefreshToken refreshToken = refreshTokenService.getRefreshToken(token).orElse(null);

        if (refreshToken == null) {
            throw new RefreshTokenException("Invalid refresh token");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenService.deleteRefreshToken(refreshToken);
            throw new RefreshTokenException("Refresh token expired. Please login again.");
        }

        refreshTokenService.deleteRefreshToken(refreshToken);

        return issueTokens(refreshToken.getUser(), response);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals("refreshToken"))
                    .findFirst()
                    .map(Cookie::getValue)
                    .flatMap(refreshTokenService::getRefreshToken)
                    .ifPresent(refreshTokenService::deleteRefreshToken);
        }
        clearRefreshTokenCookie(response); // always clear cookie regardless
    }


    //private helpers
    private String createRefreshToken(User user) {

        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token((token))
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(refreshTokenExpiration))
                .build();

        refreshTokenService.saveRefreshToken(refreshToken);

        return token;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(Duration.ofSeconds(refreshTokenExpiration))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private LoginResponse issueTokens(User user, HttpServletResponse response) {
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        String refreshToken = createRefreshToken(user);
        setRefreshTokenCookie(response, refreshToken);
        return new LoginResponse(accessToken);
    }

    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new RefreshTokenException("Refresh token missing");
        }
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst()
                .orElseThrow(() -> new RefreshTokenException("Refresh token missing"))
                .getValue();
    }

}
