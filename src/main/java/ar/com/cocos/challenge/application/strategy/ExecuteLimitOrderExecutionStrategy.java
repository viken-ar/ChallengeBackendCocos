package ar.com.cocos.challenge.application.strategy;

import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;

/**
 * Las órdenes LIMIT quedan registradas en estado NEW esperando a ser ejecutadas.
 */
public class ExecuteLimitOrderExecutionStrategy extends AbstractOrderExecutionStrategy {

    public ExecuteLimitOrderExecutionStrategy(OrderRepositoryPort orderRepository,
                                              MarketDataRepositoryPort marketDataRepositoryPort) {
        super(orderRepository, marketDataRepositoryPort);
    }

    @Override
    protected void applyExecution(Order order, OrderContext context) {
        order.fill(context.effectivePrice());
    }
}
