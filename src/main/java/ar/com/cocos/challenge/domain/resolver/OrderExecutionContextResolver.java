package ar.com.cocos.challenge.domain.resolver;

import ar.com.cocos.challenge.domain.calculator.PortfolioCalculator;
import ar.com.cocos.challenge.domain.exception.BadDataException;
import ar.com.cocos.challenge.domain.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Resuelve el contexto de ejecución: precio efectivo, saldo disponible y tenencia previa.
 * También convierte un monto de inversión en cantidad de acciones (sin fracciones).
 */
public class OrderExecutionContextResolver {

    private OrderExecutionContextResolver() {}

    public static OrderContext resolve(Order order, List<Order> filledOrders, Optional<MarketData> lastMarketDate) {
        BigDecimal availableCash = PortfolioCalculator.availableCash(filledOrders);
        int availableShares = PortfolioCalculator.netShares(filledOrders, order.getInstrumentId());

        BigDecimal effectivePrice = resolvePrice(order, lastMarketDate);

        if (order.getAmount() != null) {
            if (order.getSize() != null) {
                throw new BadDataException("Enviar 'size' o 'amount', no ambos");
            }
            int calculatedSize = order.getAmount()
                    .divide(effectivePrice, 0, RoundingMode.DOWN)
                    .intValueExact();
            if (calculatedSize <= 0) {
                throw new BadDataException("El monto enviado no alcanza para adquirir una acción");
            }
            order.setSize(calculatedSize);
        }

        if (order.getSize() == null || order.getSize() <= 0) {
            throw new BadDataException("La cantidad de acciones debe ser mayor a cero");
        }

        return new OrderContext(effectivePrice, availableCash, availableShares);
    }

    private static BigDecimal resolvePrice(Order order, Optional<MarketData> lastMarketDate) {
        if (isCashMovement(order)) {
            return BigDecimal.ONE;
        }

        if (order.getType() == OrderType.MARKET) {
            if (lastMarketDate.isEmpty() || lastMarketDate.get().close() == null) {
                throw new BadDataException("No hay cotización para el instrumento " + order.getInstrumentId());
            }
            return lastMarketDate.get().close();
        }

        BigDecimal price = order.getPrice();
        if (price == null || price.signum() <= 0) {
            throw new BadDataException("Las órdenes LIMIT requieren un precio mayor a cero");
        }
        return price;
    }

    private static boolean isCashMovement(Order order) {
        return order.getSide() == OrderSide.CASH_IN || order.getSide() == OrderSide.CASH_OUT;
    }
}
