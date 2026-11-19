package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchInstrumentsUseCaseTest {

    @Mock
    private InstrumentRepositoryPort instrumentRepositoryPort;

    @Mock
    private MarketDataRepositoryPort marketDataRepositoryPort;

    private SearchInstrumentsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchInstrumentsUseCaseImpl(instrumentRepositoryPort, marketDataRepositoryPort);
    }

    @Test
    void search_shouldReturnEnrichedInstrumentsWithCalculatedVariation() {
        Instrument instrument = new Instrument(1, "GGAL", "Grupo Financiero Galicia", InstrumentType.ACCIONES);
        MarketData marketData = new MarketData(
                10, 1,
                new BigDecimal("100.00"), new BigDecimal("90.00"),
                new BigDecimal("95.00"), new BigDecimal("110.00"),
                new BigDecimal("100.00"), LocalDate.of(2023, 7, 14)
        );

        when(instrumentRepositoryPort.search("GGAL", 1, 10)).thenReturn(new Page<>(List.of(instrument), true, false, 1 , 1));
        when(marketDataRepositoryPort.findLatestByInstrumentId(1)).thenReturn(Optional.of(marketData));

        Page<InstrumentSearchResult> results = useCase.search("GGAL", 1, 10);

        assertThat(results.content()).hasSize(1);
        InstrumentSearchResult result = results.content().getFirst();
        assertThat(result.instrument().ticker()).isEqualTo("GGAL");
        assertThat(result.lastPrice()).isEqualByComparingTo("110.00");
        assertThat(result.previousClose()).isEqualByComparingTo("100.00");
        // (110 - 100) / 100 * 100 = 10.00%
        assertThat(result.dailyVariationPercentage()).isEqualByComparingTo("10.00");
    }

    @Test
    void search_whenMarketDataNotFound_shouldHandleGracefully() {
        Instrument instrument = new Instrument(2, "UNKNOWN", "Unknown S.A.", InstrumentType.ACCIONES);

        when(instrumentRepositoryPort.search("UNKNOWN", 1, 10)).thenReturn(new Page<>(List.of(instrument), true, false, 1 , 1));
        when(marketDataRepositoryPort.findLatestByInstrumentId(2)).thenReturn(Optional.empty());

        Page<InstrumentSearchResult> results = useCase.search("UNKNOWN", 1, 10);

        assertThat(results.content()).hasSize(1);
        InstrumentSearchResult result = results.content().getFirst();
        assertThat(result.lastPrice()).isNull();
        assertThat(result.previousClose()).isNull();
        assertThat(result.dailyVariationPercentage()).isNull();
    }
}
