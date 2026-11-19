package ar.com.cocos.challenge.infrastructure.database.mappers;

import ar.com.cocos.challenge.domain.model.MarketData;
import ar.com.cocos.challenge.infrastructure.database.entities.MarketDataEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MarketDataInfrastructureMapper {
    MarketData toDomain(MarketDataEntity entity);
    MarketDataEntity toEntity(MarketData domain);
    List<MarketData> toDomainList(List<MarketDataEntity> entities);
}
