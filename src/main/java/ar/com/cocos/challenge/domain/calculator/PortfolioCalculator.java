package ar.com.cocos.challenge.domain.calculator;

import ar.com.cocos.challenge.domain.model.Order;
import ar.com.cocos.challenge.domain.model.OrderSide;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Cálculos puros derivados de las órdenes FILLED. No depende de frameworks.
 */
public final class PortfolioCalculator {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private PortfolioCalculator() {
    }

    /**
     * Pesos disponibles: CASH_IN - CASH_OUT - compras + ventas.
     */
    public static BigDecimal availableCash(List<Order> filledOrders) {
        BigDecimal cash = BigDecimal.ZERO;
        for (Order order : filledOrders) {
            if (order.getSide() == null || order.getSize() == null) {
                continue;
            }
            BigDecimal size = BigDecimal.valueOf(order.getSize());
            switch (order.getSide()) {
                case CASH_IN -> cash = cash.add(size);
                case CASH_OUT -> cash = cash.subtract(size);
                case BUY -> cash = cash.subtract(size.multiply(priceOf(order)));
                case SELL -> cash = cash.add(size.multiply(priceOf(order)));
            }
        }
        return cash;
    }

    /**
     * Cantidad neta de acciones de un instrumento: compras - ventas.
     */
    public static int netShares(List<Order> filledOrders, Integer instrumentId) {
        int shares = 0;
        for (Order order : filledOrders) {
            if (!instrumentId.equals(order.getInstrumentId()) || order.getSize() == null) {
                continue;
            }
            if (order.getSide() == OrderSide.BUY) {
                shares += order.getSize();
            } else if (order.getSide() == OrderSide.SELL) {
                shares -= order.getSize();
            }
        }
        return shares;
    }

    /**
     * Costo neto de la posición: compras - ventas.
     */
    public static BigDecimal costBasis(List<Order> filledOrders, Integer instrumentId) {
        BigDecimal cost = BigDecimal.ZERO;
        for (Order order : filledOrders) {
            if (!instrumentId.equals(order.getInstrumentId())
                    || order.getSize() == null
                    || (order.getSide() != OrderSide.BUY && order.getSide() != OrderSide.SELL)) {
                continue;
            }
            BigDecimal movement = BigDecimal.valueOf(order.getSize()).multiply(priceOf(order));
            cost = order.getSide() == OrderSide.BUY ? cost.add(movement) : cost.subtract(movement);
        }
        return cost;
    }

    public static BigDecimal percentage(BigDecimal current, BigDecimal base) {
        if (base == null || base.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return current.subtract(base)
                .multiply(HUNDRED)
                .divide(base, 4, RoundingMode.HALF_UP);
    }

    private static BigDecimal priceOf(Order order) {
        return order.getPrice() != null ? order.getPrice() : BigDecimal.ZERO;
    }
}
