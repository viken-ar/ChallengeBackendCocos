package ar.com.cocos.challenge.domain.validator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;
import ar.com.cocos.challenge.domain.model.OrderType;
import ar.com.cocos.challenge.domain.model.Result;

import java.math.BigDecimal;

/**
 * Valida el precio según el tipo de orden: MARKET usa el precio de mercado,
 * LIMIT requiere un precio explícito mayor a cero.
 */
public class PriceValidator implements OrderValidator {

    @Override
    public Result<Void> validate(Order order, OrderContext context) {
        BigDecimal effectivePrice = context.effectivePrice();
        if (effectivePrice == null || effectivePrice.signum() <= 0) {
            return Result.failure("El precio de ejecución debe ser mayor a cero");
        }
        if (order.getType() == OrderType.LIMIT && (order.getPrice() == null || order.getPrice().signum() <= 0)) {
            return Result.failure("Las órdenes LIMIT requieren un precio mayor a cero");
        }
        return Result.success(null);
    }
}
