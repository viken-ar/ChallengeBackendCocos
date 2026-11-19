package ar.com.cocos.challenge.domain.model;

import java.math.BigDecimal;

/**
 * Datos derivados necesarios para validar y ejecutar una orden.
 *
 * @param effectivePrice precio al que se ejecuta la orden (close para MARKET, precio enviado para LIMIT,
 *                       o 1 para movimientos de efectivo)
 * @param availableCash  pesos disponibles antes de la orden
 * @param availableShares acciones disponibles del instrumento antes de la orden
 */
public record OrderContext(
        BigDecimal effectivePrice,
        BigDecimal availableCash,
        int availableShares
) {
}
