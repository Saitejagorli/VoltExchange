package com.saicodes.VoltExchange.constants;

public final class Constants {

    private Constants() {}

    public static class Security{
        public static final String[] PUBLIC_ENDPOINTS = {
                "/",
                "/api/v1/auth/register",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/swagger-ui/index.html",
                "/v3/api-docs/**",
                "/swagger-ui/**"

        };
    }

}
