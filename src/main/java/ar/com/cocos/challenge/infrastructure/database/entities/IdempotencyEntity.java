package ar.com.cocos.challenge.infrastructure.database.entities;

import ar.com.cocos.challenge.domain.model.IdempotencyStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyEntity {

    @Id
    @Column(name = "\"key\"")
    private UUID key;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "\"status\"", nullable = false)
    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public IdempotencyEntity() {}

    public IdempotencyEntity(UUID key, Integer userId, String requestHash, String responseBody, IdempotencyStatus status, int statusCode, Instant createdAt) {
        this.key = key;
        this.userId = userId;
        this.requestHash = requestHash;
        this.responseBody = responseBody;
        this.status = status;
        this.statusCode = statusCode;
        this.createdAt = createdAt;
    }

    public UUID getKey() { return key; }
    public void setKey(UUID key) { this.key = key; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public IdempotencyStatus getStatus() { return status; }
    public void setStatus(IdempotencyStatus status) { this.status = status; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
