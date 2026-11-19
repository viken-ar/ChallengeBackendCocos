package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.inbound.GetUserOrderByIdUseCase;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.exception.NotFoundException;
import ar.com.cocos.challenge.domain.model.Order;

public class GetUserOrderByIdUseCaseImpl implements GetUserOrderByIdUseCase {

    private final OrderRepositoryPort orderRepositoryPort;

    public GetUserOrderByIdUseCaseImpl(
            OrderRepositoryPort orderRepositoryPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
    }

    @Override
    public Order getUserOrderById(Integer userId, Integer orderId) {
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        
        if (!order.getUserId().equals(userId)) {
            throw new NotFoundException("Order not found for user: " + userId);
        }
        
        return order;
    }
}
