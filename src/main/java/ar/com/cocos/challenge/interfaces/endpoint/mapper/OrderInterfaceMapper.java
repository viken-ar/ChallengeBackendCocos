package ar.com.cocos.challenge.interfaces.endpoint.mapper;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.interfaces.endpoint.request.CreateOrderRequest;
import ar.com.cocos.challenge.interfaces.endpoint.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface OrderInterfaceMapper {

    @Mapping(target = "idempotencyKey", source = "idempotencyKey")
    @Mapping(target = "userId", source = "request.userId")
    @Mapping(target = "instrumentId", source = "request.instrumentId")
    @Mapping(target = "side", source = "request.side")
    @Mapping(target = "type", source = "request.type")
    @Mapping(target = "size", source = "request.size")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "price", source = "request.price")
    CreateOrderCommand toCommand(UUID idempotencyKey, CreateOrderRequest request);

    @Mapping(target = "id", source = "order.id")
    @Mapping(target = "userId", source = "order.userId")
    @Mapping(target = "instrumentId", source = "order.instrumentId")
    @Mapping(target = "ticker", source = "order.instrumentName")
    @Mapping(target = "side", source = "order.side")
    @Mapping(target = "type", source = "order.type")
    @Mapping(target = "status", source = "order.status")
    @Mapping(target = "size", source = "order.size")
    @Mapping(target = "price", source = "order.price")
    @Mapping(target = "datetime", source = "order.datetime")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(order))")
    @Mapping(target = "rejectReason", source = "order.rejectReason")
    OrderResponse toResponse(Order order);

    default BigDecimal calculateTotalAmount(Order order) {
        if (order == null || order.getSize() == null || order.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(order.getSize()).multiply(order.getPrice());
    }
}
