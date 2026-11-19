package ar.com.cocos.challenge.interfaces.endpoint.request;

import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.OrderType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateOrderRequest(
    @NotNull(message = "userId is required")
    Integer userId,

    @NotNull(message = "instrumentId is required")
    Integer instrumentId,

    @NotNull(message = "side is required")
    OrderSide side,

    @NotNull(message = "type is required")
    OrderType type,

    Integer size,

    BigDecimal amount,

    BigDecimal price
) {}
