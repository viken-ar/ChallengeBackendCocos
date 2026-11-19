package ar.com.cocos.challenge.application.command;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderCommand(
    UUID idempotencyKey,
    Integer userId,
    Integer instrumentId,
    OrderSide side,
    OrderType type,
    Integer size,
    BigDecimal amount,
    BigDecimal price
) {

    public Order getOrder() {
        return Order.builder()
                .userId(userId)
                .instrumentId(instrumentId)
                .side(side)
                .type(type)
                .size(size)
                .amount(amount)
                .price(price)
                .status(OrderStatus.NEW)
                .build();
    }

}
