package ar.com.cocos.challenge.interfaces.endpoint.response;

import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
    Integer id,
    Integer userId,
    Integer instrumentId,
    String ticker,
    OrderSide side,
    OrderType type,
    OrderStatus status,
    Integer size,
    BigDecimal price,
    BigDecimal totalAmount,
    Instant datetime,
    String rejectReason
) {}
