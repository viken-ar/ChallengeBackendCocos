package ar.com.cocos.challenge.interfaces.listeners;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.application.inbound.CreateOrderUseCase;
import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.domain.events.OrderFilledEvent;
import ar.com.cocos.challenge.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Listens for {@link OrderFilledEvent} and creates the corresponding cash flow order.
 * - SELL FILLED → creates CASH_IN order
 * - BUY FILLED → creates CASH_OUT order
 */
@Component
public class CashFlowOrderListener {

    private static final Logger log = LoggerFactory.getLogger(CashFlowOrderListener.class);

    private final CreateOrderUseCase createOrderUseCase;
    private final InstrumentRepositoryPort instrumentRepositoryPort;

    public CashFlowOrderListener(
            CreateOrderUseCase createOrderUseCase,
            InstrumentRepositoryPort instrumentRepositoryPort
    ) {
        this.createOrderUseCase = createOrderUseCase;
        this.instrumentRepositoryPort = instrumentRepositoryPort;
    }

    @EventListener
    @Transactional
    public void onOrderFilled(OrderFilledEvent event) {
        Order order = event.getOrder();

        OrderSide cashSide;
        if (order.getSide() == OrderSide.SELL) {
            cashSide = OrderSide.CASH_IN;
        } else if (order.getSide() == OrderSide.BUY) {
            cashSide = OrderSide.CASH_OUT;
        } else {
            return;
        }

        Instrument cashInstrument = instrumentRepositoryPort.findByType(InstrumentType.MONEDA)
                .orElse(null);
        if (cashInstrument == null) {
            log.error("MONEDA instrument not found for user {}. Cannot create cash flow order for order {}.",
                    order.getUserId(), order.getId());
            return;
        }

        BigDecimal cashAmount = order.getPrice().multiply(BigDecimal.valueOf(order.getSize()));
        int cashSize = cashAmount.setScale(0, RoundingMode.DOWN).intValue();

        log.info("Creating {} order with size {} for order {} (user {})",
                cashSide, cashSize, order.getId(), order.getUserId());

        CreateOrderCommand command = new CreateOrderCommand(
                UUID.randomUUID(),
                order.getUserId(),
                cashInstrument.id(),
                cashSide,
                OrderType.MARKET,
                cashSize,
                null,
                null
        );

        try {
            createOrderUseCase.createOrder(command);
            log.info("Cash flow order {} created successfully for order {}", cashSide, order.getId());
        } catch (Exception e) {
            log.error("Failed to create {} order for order {}: {}", cashSide, order.getId(), e.getMessage());
        }
    }
}
