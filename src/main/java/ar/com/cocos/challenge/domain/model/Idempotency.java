package ar.com.cocos.challenge.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Idempotency(
    UUID key,
    Integer userId,
    String requestHash,
    String responseBody,
    IdempotencyStatus status,
    Instant createdAt
) {}
