package ar.com.cocos.challenge.domain.validator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;
import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.Result;

/**
 * Valida que existan acciones suficientes para una venta.
 */
public class HoldingsValidator implements OrderValidator {

    @Override
    public Result<Void> validate(Order order, OrderContext context) {
        if (order.getSide() == OrderSide.SELL && order.getSize() > context.availableShares()) {
            return Result.failure("Acciones insuficientes: se solicitan " + order.getSize()
                    + " y hay " + context.availableShares());
        }
        return Result.success(null);
    }
}
