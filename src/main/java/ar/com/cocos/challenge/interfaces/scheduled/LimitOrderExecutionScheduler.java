package ar.com.cocos.challenge.interfaces.scheduled;

import ar.com.cocos.challenge.application.outbound.EventPublisherPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.strategy.ExecuteLimitOrderExecutionStrategy;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tarea programada que se ejecuta cada minuto para procesar las órdenes LIMIT con estado NEW.
 * Busca las órdenes pendientes y ejecuta la estrategia {@link ExecuteLimitOrderExecutionStrategy}.
 */
@Component
public class LimitOrderExecutionScheduler {

    private static final Logger log = LoggerFactory.getLogger(LimitOrderExecutionScheduler.class);

    private final OrderRepositoryPort orderRepositoryPort;
    private final ExecuteLimitOrderExecutionStrategy executeLimitOrderExecutionStrategy;

    private final EventPublisherPort eventPublisherPort;

    public LimitOrderExecutionScheduler(
            OrderRepositoryPort orderRepositoryPort,
            ExecuteLimitOrderExecutionStrategy executeLimitOrderExecutionStrategy, EventPublisherPort eventPublisherPort
    ) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.executeLimitOrderExecutionStrategy = executeLimitOrderExecutionStrategy;
        this.eventPublisherPort = eventPublisherPort;
    }

    @Scheduled(fixedRate = 60000)
    public void executeLimitOrders() {
        List<Order> limitOrdersNew = orderRepositoryPort.findByTypeAndStatus(OrderType.LIMIT, OrderStatus.NEW);

        if (limitOrdersNew.isEmpty()) {
            log.debug("No hay órdenes LIMIT con estado NEW para procesar.");
            return;
        }

        log.info("Procesando {} órdenes LIMIT con estado NEW.", limitOrdersNew.size());

        for (Order order : limitOrdersNew) {
            try {
                log.info("Ejecutando estrategia para orden LIMIT id={}, userId={}, instrumentId={}",
                        order.getId(), order.getUserId(), order.getInstrumentId());

                var result = executeLimitOrderExecutionStrategy.execute(order);

                if (result.isSuccess()) {
                    log.info("Orden LIMIT id={} procesada exitosamente. Nuevo estado: {}",
                            order.getId(), result.getData().getStatus());

                    var orderSneder = result.getData();

                    if ((orderSneder.getSide() == OrderSide.BUY || orderSneder.getSide() == OrderSide.SELL)
                            && orderSneder.getStatus() == OrderStatus.FILLED) {
                        eventPublisherPort.publish(orderSneder);
                    }

                } else {
                    log.error("Orden LIMIT id={} falló al procesarse: {}",
                            order.getId(), result.getError());
                }
            } catch (Exception e) {
                log.error("Error al procesar orden LIMIT id={}: {}", order.getId(), e.getMessage(), e);
            }
        }

        log.info("Finalizó el procesamiento de órdenes LIMIT.");
    }
}
