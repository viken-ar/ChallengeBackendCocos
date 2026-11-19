package ar.com.cocos.challenge.infrastructure.database.mappers;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.infrastructure.database.entities.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderInfrastructureMapper {
    @Mapping(target = "instrumentId", source = "entity.instrument.id")
    @Mapping(target = "instrumentName", source = "entity.instrument.ticker")
    Order toDomain(OrderEntity entity);
    @Mapping(target = "instrument", ignore = true)
    OrderEntity toEntity(Order domain);
    List<Order> toDomainList(List<OrderEntity> entities);
}
