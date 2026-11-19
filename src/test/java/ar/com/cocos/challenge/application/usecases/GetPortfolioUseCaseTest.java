package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.outbound.UserRepositoryPort;
import ar.com.cocos.challenge.domain.exception.NotFoundException;
import ar.com.cocos.challenge.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPortfolioUseCaseTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private InstrumentRepositoryPort instrumentRepositoryPort;

    @Mock
    private MarketDataRepositoryPort marketDataRepositoryPort;

    private GetPortfolioUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPortfolioUseCaseImpl(
                userRepositoryPort,
                orderRepositoryPort,
                instrumentRepositoryPort,
                marketDataRepositoryPort
        );
    }

    @Test
    void getPortfolio_whenUserNotFound_shouldThrowNotFoundException() {
        when(userRepositoryPort.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getPortfolio(99))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getPortfolio_withFilledOrders_shouldCalculateAccurateBalancesAndPositions() {
        Integer userId = 1;
        User user = new User(userId, "test@test.com", "10001");
        when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));

        // CASH_IN 1,000,000
        Order cashIn = new Order(1, 66, "CASH", userId, 1000000, BigDecimal.ONE, OrderType.MARKET, OrderSide.CASH_IN, OrderStatus.FILLED, Instant.now(), null, BigDecimal.ZERO);
        // BUY 100 shares of instrument 10 @ 50.00 = 5,000
        Order buy1 = new Order(2, 10, "PAMP", userId, 100, new BigDecimal("50.00"), OrderType.MARKET, OrderSide.BUY, OrderStatus.FILLED, Instant.now(), null, BigDecimal.ZERO);
        // BUY 100 shares of instrument 10 @ 60.00 = 6,000 (avg cost = 55.00 for 200 shares)
        Order buy2 = new Order(3, 10, "PAMP", userId, 100, new BigDecimal("60.00"), OrderType.LIMIT, OrderSide.BUY, OrderStatus.FILLED, Instant.now(), null, BigDecimal.ZERO);
        // SELL 50 shares of instrument 10 @ 70.00 = 3,500 (150 shares remaining)
        Order sell1 = new Order(4, 10, "PAMP", userId, 50, new BigDecimal("70.00"), OrderType.MARKET, OrderSide.SELL, OrderStatus.FILLED, Instant.now(), null, BigDecimal.ZERO);
        // CASH_OUT 50,000
        Order cashOut = new Order(5, 66, "CASH", userId, 50000, BigDecimal.ONE, OrderType.MARKET, OrderSide.CASH_OUT, OrderStatus.FILLED, Instant.now(), null, BigDecimal.ZERO);

        when(orderRepositoryPort.findByUserIdAndStatus(userId, OrderStatus.FILLED))
                .thenReturn(List.of(cashIn, buy1, buy2, sell1, cashOut));

        Instrument inst10 = new Instrument(10, "PAMP", "Pampa Energia", InstrumentType.ACCIONES);
        when(instrumentRepositoryPort.findById(10)).thenReturn(Optional.of(inst10));

        // Current close = 70.00, previousClose = 65.00
        MarketData md = new MarketData(
                1, 10,
                new BigDecimal("72.00"), new BigDecimal("64.00"),
                new BigDecimal("66.00"), new BigDecimal("70.00"),
                new BigDecimal("65.00"), LocalDate.now()
        );
        when(marketDataRepositoryPort.findLatestByInstrumentId(10)).thenReturn(Optional.of(md));

        Portfolio portfolio = useCase.getPortfolio(userId);

        assertThat(portfolio.userId()).isEqualTo(userId);
        assertThat(portfolio.accountNumber()).isEqualTo("10001");

        // Expected cash: 1,000,000 - 5,000 - 6,000 + 3,500 - 50,000 = 942,500.00
        assertThat(portfolio.availableCash()).isEqualByComparingTo("942500.00");

        // Expected position: 150 shares @ 70.00 = 10,500.00
        assertThat(portfolio.positions()).hasSize(1);
        Position position = portfolio.positions().getFirst();
        assertThat(position.sharesCount()).isEqualTo(150);
        assertThat(position.currentPrice()).isEqualByComparingTo("70.00");
        assertThat(position.totalValue()).isEqualByComparingTo("10500.00");

        // Daily return: (70 - 65) / 65 * 100 = 7.69%
        assertThat(position.dailyReturnPercentage()).isEqualByComparingTo("7.69");

        // Total return: total bought 200 shares for 11,000 -> avg buy = 55.00. Cost basis = 150 * 55 = 8,250.
        // Total return = (10,500 - 8,250) / 8,250 * 100 = 27.27%
        assertThat(position.totalReturnPercentage()).isEqualByComparingTo("27.27");

        // Total account value: 942,500 + 10,500 = 953,000.00
        assertThat(portfolio.totalAccountValue()).isEqualByComparingTo("953000.00");
    }
}
