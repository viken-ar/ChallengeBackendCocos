package ar.com.cocos.challenge.domain.validator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;
import ar.com.cocos.challenge.domain.model.Result;

/**
 * Valida que la orden tenga una cantidad de acciones positiva.
 */
public class SizeValidator implements OrderValidator {

    @Override
    public Result<Void> validate(Order order, OrderContext context) {
        if (order.getSize() == null || order.getSize() <= 0) {
            return Result.failure("La cantidad de acciones debe ser mayor a cero");
        }
        return Result.success(null);
    }
}
