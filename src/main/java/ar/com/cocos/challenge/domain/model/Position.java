package ar.com.cocos.challenge.domain.model;

import java.math.BigDecimal;

public record Position(
    Integer instrumentId,
    String ticker,
    String name,
    Integer sharesCount,
    BigDecimal currentPrice,
    BigDecimal totalValue,
    BigDecimal dailyReturnPercentage,
    BigDecimal totalReturnPercentage
) {}
