package ar.com.cocos.challenge.domain.model;

import java.math.BigDecimal;

public record InstrumentSearchResult(
    Instrument instrument,
    MarketData marketData,
    BigDecimal lastPrice,
    BigDecimal previousClose,
    BigDecimal dailyVariationPercentage
) {}
