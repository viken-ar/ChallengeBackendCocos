package ar.com.cocos.challenge.application.strategy;

import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.exception.BadDataException;
import ar.com.cocos.challenge.domain.model.*;
import ar.com.cocos.challenge.domain.resolver.OrderExecutionContextResolver;
import ar.com.cocos.challenge.domain.validator.*;

import java.util.List;
import java.util.Optional;

/**
 * Template Method: fija el esqueleto común de ejecución de una orden.
 * Las subclases definen únicamente cómo se aplica la ejecución (hook).
 */
public abstract class AbstractOrderExecutionStrategy implements OrderExecutionStrategy {

    private final OrderValidatorChain validatorChain;
    private final OrderRepositoryPort orderRepository;
    private final MarketDataRepositoryPort marketDataRepositoryPort;

    protected AbstractOrderExecutionStrategy(OrderRepositoryPort orderRepository,
                                             MarketDataRepositoryPort marketDataRepositoryPort) {
        this.validatorChain = new OrderValidatorChain(List.of(new SizeValidator(),
                new PriceValidator(),
                new FundsValidator(),
                new HoldingsValidator()));
        this.orderRepository = orderRepository;
        this.marketDataRepositoryPort = marketDataRepositoryPort;
    }

    @Override
    public final Result<Order> execute(Order order) {

        try {
            var filledOrders = orderRepository.findByUserIdAndStatus(order.getUserId(), OrderStatus.FILLED);
            Optional<MarketData> lastMarketDate = marketDataRepositoryPort.findLatestByInstrumentId(order.getInstrumentId());
            OrderContext context = OrderExecutionContextResolver.resolve(order, filledOrders, lastMarketDate);
            Result<Void> validation = validatorChain.validate(order, context);

            if (validation.isFailure()) {
                order.reject(validation.getError());
                return Result.success(orderRepository.save(order));
            }

            applyExecution(order, context);
            return Result.success(orderRepository.save(order));
        } catch (Exception e) {
            return Result.failure(e.getMessage());
        }

    }

    /**
     * Hook del Template Method: MARKET executa y pasa a FILLED; LIMIT permanece NEW.
     */
    protected abstract void applyExecution(Order order, OrderContext context);
}
