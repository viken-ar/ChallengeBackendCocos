package ar.com.cocos.challenge.domain.validator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;
import ar.com.cocos.challenge.domain.model.Result;

import java.math.BigDecimal;

/**
 * Valida que existan pesos suficientes para BUY o CASH_OUT.
 */
public class FundsValidator implements OrderValidator {

    @Override
    public Result<Void> validate(Order order, OrderContext context) {
        BigDecimal required = switch (order.getSide()) {
            case BUY -> BigDecimal.valueOf(order.getSize()).multiply(context.effectivePrice());
            case CASH_OUT -> BigDecimal.valueOf(order.getSize());
            default -> BigDecimal.ZERO;
        };

        if (required.signum() > 0 && required.compareTo(context.availableCash()) > 0) {
            return Result.failure("Fondos insuficientes: se requieren " + required
                    + " y están disponibles " + context.availableCash());
        }
        return Result.success(null);
    }
}
