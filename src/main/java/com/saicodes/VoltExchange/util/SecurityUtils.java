package com.saicodes.VoltExchange.util;

import com.saicodes.VoltExchange.entities.User;
import com.saicodes.VoltExchange.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {
    private final UserService userService;
    public User getCurrentUser() {
        String email = SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();
        return userService.getUserByEmail(email);
    }

}
