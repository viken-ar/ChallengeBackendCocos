package ar.com.cocos.challenge.interfaces.endpoint.response;

import ar.com.cocos.challenge.domain.model.InstrumentType;
import java.math.BigDecimal;

public record InstrumentResponse(
    Integer id,
    String ticker,
    String name,
    InstrumentType type,
    BigDecimal lastPrice,
    BigDecimal previousClose,
    BigDecimal dailyVariationPercentage
) {}
