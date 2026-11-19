package ar.com.cocos.challenge.infrastructure.database.repositories;

import ar.com.cocos.challenge.infrastructure.database.entities.IdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyEntity, UUID> {
}
