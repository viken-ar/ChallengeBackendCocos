package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.inbound.GetPortfolioUseCase;
import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.application.outbound.OrderRepositoryPort;
import ar.com.cocos.challenge.application.outbound.UserRepositoryPort;
import ar.com.cocos.challenge.domain.exception.NotFoundException;
import ar.com.cocos.challenge.domain.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class GetPortfolioUseCaseImpl implements GetPortfolioUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final OrderRepositoryPort orderRepositoryPort;
    private final InstrumentRepositoryPort instrumentRepositoryPort;
    private final MarketDataRepositoryPort marketDataRepositoryPort;

    public GetPortfolioUseCaseImpl(
            UserRepositoryPort userRepositoryPort,
            OrderRepositoryPort orderRepositoryPort,
            InstrumentRepositoryPort instrumentRepositoryPort,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.orderRepositoryPort = orderRepositoryPort;
        this.instrumentRepositoryPort = instrumentRepositoryPort;
        this.marketDataRepositoryPort = marketDataRepositoryPort;
    }

    @Override
    public Portfolio getPortfolio(Integer userId) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new NotFoundException(User.class, userId));

        List<Order> filledOrders = orderRepositoryPort.findByUserIdAndStatus(userId, OrderStatus.FILLED);

        BigDecimal availableCash = calculateAvailableCash(filledOrders);
        List<Position> positions = calculatePositions(filledOrders);

        BigDecimal positionsValue = positions.stream()
                .map(Position::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAccountValue = availableCash.add(positionsValue).setScale(2, RoundingMode.HALF_UP);

        return new Portfolio(
                user.id(),
                user.accountNumber(),
                totalAccountValue,
                availableCash,
                positions
        );
    }

    private BigDecimal calculateAvailableCash(List<Order> orders) {
        BigDecimal cash = BigDecimal.ZERO;
        for (Order order : orders) {
            BigDecimal orderAmount = BigDecimal.valueOf(order.getSize()).multiply(order.getPrice());
            if (order.getSide() == OrderSide.CASH_IN || order.getSide() == OrderSide.SELL) {
                cash = cash.add(orderAmount);
            } else if (order.getSide() == OrderSide.CASH_OUT || order.getSide() == OrderSide.BUY) {
                cash = cash.subtract(orderAmount);
            }
        }
        return cash.setScale(2, RoundingMode.HALF_UP);
    }

    private List<Position> calculatePositions(List<Order> orders) {
        Map<Integer, List<Order>> ordersByInstrument = orders.stream()
                .filter(o -> o.getSide() == OrderSide.BUY || o.getSide() == OrderSide.SELL)
                .collect(Collectors.groupingBy(Order::getInstrumentId));

        List<Position> positions = new ArrayList<>();

        for (Map.Entry<Integer, List<Order>> entry : ordersByInstrument.entrySet()) {
            Integer instrumentId = entry.getKey();
            List<Order> instrumentOrders = entry.getValue();

            int totalBought = 0;
            BigDecimal totalBuyCost = BigDecimal.ZERO;
            int totalSold = 0;

            for (Order o : instrumentOrders) {
                if (o.getSide() == OrderSide.BUY) {
                    totalBought += o.getSize();
                    totalBuyCost = totalBuyCost.add(BigDecimal.valueOf(o.getSize()).multiply(o.getPrice()));
                } else if (o.getSide() == OrderSide.SELL) {
                    totalSold += o.getSize();
                }
            }

            int netShares = totalBought - totalSold;
            if (netShares <= 0) {
                continue;
            }

            Instrument instrument = instrumentRepositoryPort.findById(instrumentId)
                    .orElse(new Instrument(instrumentId, "N/A", "Unknown", InstrumentType.ACCIONES));

            if (instrument.type() == InstrumentType.MONEDA || "ARS".equalsIgnoreCase(instrument.ticker())) {
                continue;
            }

            Optional<MarketData> marketDataOpt = marketDataRepositoryPort.findLatestByInstrumentId(instrumentId);
            BigDecimal currentPrice = marketDataOpt.map(MarketData::close).orElse(BigDecimal.ZERO);
            BigDecimal previousClose = marketDataOpt.map(MarketData::previousClose).orElse(BigDecimal.ZERO);

            BigDecimal totalValue = BigDecimal.valueOf(netShares).multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);

            BigDecimal dailyReturnPercentage = BigDecimal.ZERO;
            if (previousClose.compareTo(BigDecimal.ZERO) > 0 && currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                dailyReturnPercentage = currentPrice.subtract(previousClose)
                        .divide(previousClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal totalReturnPercentage = BigDecimal.ZERO;
            if (totalBought > 0) {
                BigDecimal avgBuyPrice = totalBuyCost.divide(BigDecimal.valueOf(totalBought), 4, RoundingMode.HALF_UP);
                BigDecimal costBasis = avgBuyPrice.multiply(BigDecimal.valueOf(netShares));
                if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                    totalReturnPercentage = totalValue.subtract(costBasis)
                            .divide(costBasis, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                }
            }

            positions.add(new Position(
                    instrument.id(),
                    instrument.ticker(),
                    instrument.name(),
                    netShares,
                    currentPrice.setScale(2, RoundingMode.HALF_UP),
                    totalValue,
                    dailyReturnPercentage,
                    totalReturnPercentage
            ));
        }

        positions.sort(Comparator.comparing(Position::totalValue).reversed());
        return positions;
    }
}
