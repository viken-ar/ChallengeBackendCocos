package ar.com.cocos.challenge.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record Portfolio(
    Integer userId,
    String accountNumber,
    BigDecimal totalAccountValue,
    BigDecimal availableCash,
    List<Position> positions
) {}
