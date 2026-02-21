package com.saicodes.VoltExchange.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id",  referencedColumnName = "id")
    private User user;

    private String token;

    private Instant createdAt;

    private Instant expiresAt;

    private boolean revoked;
}
