package ar.com.cocos.challenge.domain.validator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;
import ar.com.cocos.challenge.domain.model.Result;

import java.util.List;

/**
 * Chain of Responsibility: ejecuta los validadores en orden y corta en el primer fallo.
 */
public class OrderValidatorChain {

    private final List<OrderValidator> validators;

    public OrderValidatorChain(List<OrderValidator> validators) {
        this.validators = List.copyOf(validators);
    }

    public Result<Void> validate(Order order, OrderContext context) {
        for (OrderValidator validator : validators) {
            Result<Void> result = validator.validate(order, context);
            if (result.isFailure()) {
                return result;
            }
        }
        return Result.success(null);
    }
}
