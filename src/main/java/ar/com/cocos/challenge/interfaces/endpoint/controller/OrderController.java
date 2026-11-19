package ar.com.cocos.challenge.interfaces.endpoint.controller;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.application.inbound.CancelOrderUseCase;
import ar.com.cocos.challenge.application.inbound.CreateOrderUseCase;
import ar.com.cocos.challenge.application.inbound.GetUserOrderByIdUseCase;
import ar.com.cocos.challenge.application.inbound.GetUserOrdersUseCase;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.Page;
import ar.com.cocos.challenge.interfaces.endpoint.mapper.OrderInterfaceMapper;
import ar.com.cocos.challenge.interfaces.endpoint.request.CreateOrderRequest;
import ar.com.cocos.challenge.interfaces.endpoint.resources.OrderResource;
import ar.com.cocos.challenge.interfaces.endpoint.response.OrderResponse;
import ar.com.cocos.challenge.interfaces.endpoint.response.PageResponse;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController implements OrderResource {

    private final CreateOrderUseCase createOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final GetUserOrdersUseCase getUserOrdersUseCase;
    private final GetUserOrderByIdUseCase getUserOrderByIdUseCase;
    private final OrderInterfaceMapper orderInterfaceMapper;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            CancelOrderUseCase cancelOrderUseCase,
            GetUserOrdersUseCase getUserOrdersUseCase,
            GetUserOrderByIdUseCase getUserOrderByIdUseCase,
            OrderInterfaceMapper orderInterfaceMapper
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.getUserOrdersUseCase = getUserOrdersUseCase;
        this.getUserOrderByIdUseCase = getUserOrderByIdUseCase;
        this.orderInterfaceMapper = orderInterfaceMapper;
    }

    @Override
    public OrderResponse createOrder(UUID idempotencyKey, CreateOrderRequest request) {
        CreateOrderCommand command = orderInterfaceMapper.toCommand(idempotencyKey, request);
        Order order = createOrderUseCase.createOrder(command);
        return orderInterfaceMapper.toResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Integer orderId) {
        Order order = cancelOrderUseCase.cancelOrder(orderId);
        return orderInterfaceMapper.toResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getUserOrders(Integer userId, int page, int limit) {
        Page<Order> ordersPage = getUserOrdersUseCase.getUserOrders(userId, page, limit);
        List<OrderResponse> content = ordersPage.content().stream()
                .map(orderInterfaceMapper::toResponse)
                .toList();
        return new PageResponse<>(
                content,
                ordersPage.isFirst(),
                ordersPage.isLast(),
                ordersPage.totalElements(),
                ordersPage.totalPages()
        );
    }

    @Override
    public OrderResponse getUserOrderById(Integer userId, Integer orderId) {
        Order order = getUserOrderByIdUseCase.getUserOrderById(userId, orderId);
        return orderInterfaceMapper.toResponse(order);
    }
}
