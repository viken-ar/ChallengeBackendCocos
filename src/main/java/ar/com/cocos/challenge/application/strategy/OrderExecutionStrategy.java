package ar.com.cocos.challenge.application.strategy;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.Result;

/**
 * Strategy de ejecución de una orden. Implementaciones concretas por tipo de orden.
 */
public interface OrderExecutionStrategy {

    Result<Order> execute(Order order);
}
