package com.saicodes.VoltExchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class VoltExchangeApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoltExchangeApplication.class, args);
    }

}
