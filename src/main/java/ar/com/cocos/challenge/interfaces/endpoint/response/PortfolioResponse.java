package ar.com.cocos.challenge.interfaces.endpoint.response;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioResponse(
    Integer userId,
    String accountNumber,
    BigDecimal totalAccountValue,
    BigDecimal availableCash,
    List<PositionResponse> positions
) {}
