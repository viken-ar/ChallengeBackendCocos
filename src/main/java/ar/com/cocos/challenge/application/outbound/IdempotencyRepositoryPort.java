package ar.com.cocos.challenge.application.outbound;

import ar.com.cocos.challenge.domain.model.Idempotency;
import ar.com.cocos.challenge.domain.model.IdempotencyStatus;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepositoryPort {
    Optional<Idempotency> findByKey(UUID key);
    Idempotency save(Idempotency record);
    void updateStatus(UUID key, IdempotencyStatus status, String responseBody);
}
