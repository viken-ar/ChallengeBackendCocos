package ar.com.cocos.challenge.application.strategy;

import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;

/**
 * Las órdenes MARKET se ejecutan de inmediato al último precio de mercado.
 */
public class MarketOrderExecutionStrategy extends AbstractOrderExecutionStrategy {

    public MarketOrderExecutionStrategy(OrderRepositoryPort orderRepository,
                                        MarketDataRepositoryPort marketDataRepositoryPort) {
        super(orderRepository, marketDataRepositoryPort);
    }

    @Override
    protected void applyExecution(Order order, OrderContext context) {
        order.fill(context.effectivePrice());
    }
}
