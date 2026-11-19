package ar.com.cocos.challenge.infrastructure.database.adapter;

import ar.com.cocos.challenge.application.outbound.IdempotencyRepositoryPort;
import ar.com.cocos.challenge.domain.model.Idempotency;
import ar.com.cocos.challenge.domain.model.IdempotencyStatus;
import ar.com.cocos.challenge.infrastructure.database.entities.IdempotencyEntity;
import ar.com.cocos.challenge.infrastructure.database.mappers.IdempotencyInfrastructureMapper;
import ar.com.cocos.challenge.infrastructure.database.repositories.IdempotencyJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class IdempotencyRepositoryAdapter implements IdempotencyRepositoryPort {

    private final IdempotencyJpaRepository repository;
    private final IdempotencyInfrastructureMapper mapper;

    public IdempotencyRepositoryAdapter(IdempotencyJpaRepository repository, IdempotencyInfrastructureMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Idempotency> findByKey(UUID key) {
        return repository.findById(key).map(mapper::toDomain);
    }

    @Override
    public Idempotency save(Idempotency record) {
        IdempotencyEntity entity = mapper.toEntity(record);
        IdempotencyEntity saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void updateStatus(UUID key, IdempotencyStatus status, String responseBody) {
        repository.findById(key).ifPresent(entity -> {
            entity.setStatus(status);
            if (responseBody != null) {
                entity.setResponseBody(responseBody);
            }
            repository.save(entity);
        });
    }
}
