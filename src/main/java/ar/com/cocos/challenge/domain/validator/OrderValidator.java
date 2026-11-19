package ar.com.cocos.challenge.domain.validator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;
import ar.com.cocos.challenge.domain.model.Result;

/**
 * Eslabón de la Chain of Responsibility de validación de órdenes.
 */
public interface OrderValidator {

    Result<Void> validate(Order order, OrderContext context);
}
