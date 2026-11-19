package ar.com.cocos.challenge.infrastructure.database.mappers;

import ar.com.cocos.challenge.domain.model.Idempotency;
import ar.com.cocos.challenge.infrastructure.database.entities.IdempotencyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IdempotencyInfrastructureMapper {
    Idempotency toDomain(IdempotencyEntity entity);
    IdempotencyEntity toEntity(Idempotency domain);
}
