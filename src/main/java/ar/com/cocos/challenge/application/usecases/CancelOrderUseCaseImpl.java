package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.inbound.CancelOrderUseCase;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.exception.BadDataException;
import ar.com.cocos.challenge.domain.exception.NotFoundException;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.Result;

public class CancelOrderUseCaseImpl implements CancelOrderUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public CancelOrderUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public Order cancelOrder(Integer orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new NotFoundException(Order.class, orderId));

        Result<Void> resultTransition = order.cancel();
        if (resultTransition.isFailure()) {
            throw new BadDataException("Cannot cancel order with status " + order.getStatus() + ". Only NEW orders can be cancelled.");
        }

        Order savedOrder = orderRepositoryPort.save(order);
        return savedOrder;
    }
}
