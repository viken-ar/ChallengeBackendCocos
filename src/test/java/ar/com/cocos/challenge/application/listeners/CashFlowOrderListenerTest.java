package ar.com.cocos.challenge.application.listeners;

import ar.com.cocos.challenge.application.command.CreateOrderCommand;
import ar.com.cocos.challenge.application.inbound.CreateOrderUseCase;
import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.domain.events.OrderFilledEvent;
import ar.com.cocos.challenge.domain.model.*;
import ar.com.cocos.challenge.interfaces.listeners.CashFlowOrderListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashFlowOrderListenerTest {

    @Mock
    private CreateOrderUseCase createOrderUseCase;

    @Mock
    private InstrumentRepositoryPort instrumentRepositoryPort;

    private CashFlowOrderListener listener;

    @BeforeEach
    void setUp() {
        listener = new CashFlowOrderListener(createOrderUseCase, instrumentRepositoryPort);
    }

    @Test
    void onOrderFilled_whenSell_shouldCreateCashInOrder() {
        Order soldOrder = Order.builder()
                .id(1)
                .userId(1)
                .instrumentId(10)
                .size(5)
                .price(new BigDecimal("200.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.SELL)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        Instrument cashInstrument = new Instrument(1, "ARS", "Peso Argentino", InstrumentType.MONEDA);
        when(instrumentRepositoryPort.findByType(InstrumentType.MONEDA)).thenReturn(Optional.of(cashInstrument));
        when(createOrderUseCase.createOrder(any(CreateOrderCommand.class))).thenReturn(null);

        listener.onOrderFilled(new OrderFilledEvent(soldOrder));

        ArgumentCaptor<CreateOrderCommand> captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderUseCase).createOrder(captor.capture());

        CreateOrderCommand command = captor.getValue();
        assertThat(command.side()).isEqualTo(OrderSide.CASH_IN);
        assertThat(command.instrumentId()).isEqualTo(1);
        assertThat(command.userId()).isEqualTo(1);
        assertThat(command.size()).isEqualTo(1000); // 5 * 200 = 1000
    }

    @Test
    void onOrderFilled_whenBuy_shouldCreateCashOutOrder() {
        Order boughtOrder = Order.builder()
                .id(2)
                .userId(1)
                .instrumentId(10)
                .size(3)
                .price(new BigDecimal("150.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.BUY)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        Instrument cashInstrument = new Instrument(1, "ARS", "Peso Argentino", InstrumentType.MONEDA);
        when(instrumentRepositoryPort.findByType(InstrumentType.MONEDA)).thenReturn(Optional.of(cashInstrument));
        when(createOrderUseCase.createOrder(any(CreateOrderCommand.class))).thenReturn(null);

        listener.onOrderFilled(new OrderFilledEvent(boughtOrder));

        ArgumentCaptor<CreateOrderCommand> captor = ArgumentCaptor.forClass(CreateOrderCommand.class);
        verify(createOrderUseCase).createOrder(captor.capture());

        CreateOrderCommand command = captor.getValue();
        assertThat(command.side()).isEqualTo(OrderSide.CASH_OUT);
        assertThat(command.instrumentId()).isEqualTo(1);
        assertThat(command.userId()).isEqualTo(1);
        assertThat(command.size()).isEqualTo(450); // 3 * 150 = 450
    }

    @Test
    void onOrderFilled_whenCashInstrumentNotFound_shouldNotCreateOrder() {
        Order soldOrder = Order.builder()
                .id(3)
                .userId(1)
                .instrumentId(10)
                .size(5)
                .price(new BigDecimal("200.00"))
                .type(OrderType.MARKET)
                .side(OrderSide.SELL)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        when(instrumentRepositoryPort.findByType(InstrumentType.MONEDA)).thenReturn(Optional.empty());

        listener.onOrderFilled(new OrderFilledEvent(soldOrder));

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    void onOrderFilled_whenCashInSide_shouldIgnore() {
        Order cashInOrder = Order.builder()
                .id(4)
                .userId(1)
                .instrumentId(1)
                .size(1000)
                .price(BigDecimal.ONE)
                .type(OrderType.MARKET)
                .side(OrderSide.CASH_IN)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        listener.onOrderFilled(new OrderFilledEvent(cashInOrder));

        verify(createOrderUseCase, never()).createOrder(any());
    }

    @Test
    void onOrderFilled_whenCashOutSide_shouldIgnore() {
        Order cashOutOrder = Order.builder()
                .id(5)
                .userId(1)
                .instrumentId(1)
                .size(500)
                .price(BigDecimal.ONE)
                .type(OrderType.MARKET)
                .side(OrderSide.CASH_OUT)
                .status(OrderStatus.FILLED)
                .datetime(Instant.now())
                .build();

        listener.onOrderFilled(new OrderFilledEvent(cashOutOrder));

        verify(createOrderUseCase, never()).createOrder(any());
    }
}
