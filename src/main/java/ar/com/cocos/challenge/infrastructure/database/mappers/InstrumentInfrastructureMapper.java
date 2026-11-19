package ar.com.cocos.challenge.infrastructure.database.mappers;

import ar.com.cocos.challenge.domain.model.Instrument;
import ar.com.cocos.challenge.infrastructure.database.entities.InstrumentEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstrumentInfrastructureMapper {
    Instrument toDomain(InstrumentEntity entity);
    InstrumentEntity toEntity(Instrument domain);
    List<Instrument> toDomainList(List<InstrumentEntity> entities);
}
