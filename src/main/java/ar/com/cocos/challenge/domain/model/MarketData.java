package ar.com.cocos.challenge.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MarketData(
    Integer id,
    Integer instrumentId,
    BigDecimal high,
    BigDecimal low,
    BigDecimal open,
    BigDecimal close,
    BigDecimal previousClose,
    LocalDate date
) {}
