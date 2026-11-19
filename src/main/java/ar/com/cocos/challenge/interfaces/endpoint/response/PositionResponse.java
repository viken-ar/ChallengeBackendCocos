package ar.com.cocos.challenge.interfaces.endpoint.response;

import java.math.BigDecimal;

public record PositionResponse(
    Integer instrumentId,
    String ticker,
    String name,
    Integer size,
    BigDecimal currentPrice,
    BigDecimal totalMonetaryValue,
    BigDecimal dailyReturnPercentage,
    BigDecimal totalReturnPercentage
) {}
