package ar.com.cocos.challenge.application.strategy;

import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderContext;

/**
 * Las órdenes LIMIT quedan registradas en estado NEW esperando a ser ejecutadas.
 */
public class CreateLimitOrderExecutionStrategy extends AbstractOrderExecutionStrategy {

    public CreateLimitOrderExecutionStrategy(OrderRepositoryPort orderRepository,
                                             MarketDataRepositoryPort marketDataRepositoryPort) {
        super(orderRepository, marketDataRepositoryPort);
    }

    @Override
    protected void applyExecution(Order order, OrderContext context) {
        // Una orden LIMIT no se ejecuta al enviarla: permanece en NEW.
    }
}
