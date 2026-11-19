package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.domain.exception.BadDataException;
import ar.com.cocos.challenge.domain.exception.NotFoundException;
import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderSide;
import ar.com.cocos.challenge.domain.model.OrderStatus;
import ar.com.cocos.challenge.domain.model.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelOrderUseCaseTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    private CancelOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelOrderUseCaseImpl(orderRepositoryPort);
    }

    @Test
    void cancelOrder_whenOrderIsNew_shouldTransitionToCancelled() {
        Order order = new Order(10, 1, "GGAL", 1, 10, new BigDecimal("100.00"), OrderType.LIMIT, OrderSide.BUY, OrderStatus.NEW, Instant.now(), null, BigDecimal.ZERO);

        when(orderRepositoryPort.findById(10)).thenReturn(Optional.of(order));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = useCase.cancelOrder(10);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.getInstrumentName()).isEqualTo("GGAL");
    }

    @Test
    void cancelOrder_whenOrderNotFound_shouldThrowNotFoundException() {
        when(orderRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.cancelOrder(99))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void cancelOrder_whenOrderIsFilled_shouldThrowBadDataException() {
        Order order = new Order(11, 1, "GGAL", 1, 10, new BigDecimal("100.00"), OrderType.MARKET, OrderSide.BUY, OrderStatus.FILLED,Instant.now(), null, BigDecimal.ZERO);
        when(orderRepositoryPort.findById(11)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.cancelOrder(11))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Cannot cancel order with status FILLED");
    }

    @Test
    void cancelOrder_whenOrderIsRejected_shouldThrowBadDataException() {
        Order order = new Order(12, 1, "GGAL", 1, 10, new BigDecimal("100.00"), OrderType.MARKET, OrderSide.BUY, OrderStatus.REJECTED,Instant.now(), "REJECTED", BigDecimal.ZERO);
        when(orderRepositoryPort.findById(12)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.cancelOrder(12))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Cannot cancel order with status REJECTED");
    }

    @Test
    void cancelOrder_whenOrderIsAlreadyCancelled_shouldThrowBadDataException() {
        Order order = new Order(13, 1, "GGAL", 1, 10, new BigDecimal("100.00"), OrderType.LIMIT, OrderSide.BUY, OrderStatus.CANCELLED, Instant.now(), null, BigDecimal.ZERO);
        when(orderRepositoryPort.findById(13)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> useCase.cancelOrder(13))
                .isInstanceOf(BadDataException.class)
                .hasMessageContaining("Cannot cancel order with status CANCELLED");
    }
}
