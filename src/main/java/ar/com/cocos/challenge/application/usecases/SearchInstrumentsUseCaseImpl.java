package ar.com.cocos.challenge.application.usecases;

import ar.com.cocos.challenge.application.inbound.SearchInstrumentsUseCase;
import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.domain.model.Instrument;
import ar.com.cocos.challenge.domain.model.InstrumentSearchResult;
import ar.com.cocos.challenge.domain.model.MarketData;
import ar.com.cocos.challenge.domain.model.Page;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class SearchInstrumentsUseCaseImpl implements SearchInstrumentsUseCase {

    private final InstrumentRepositoryPort instrumentRepositoryPort;
    private final MarketDataRepositoryPort marketDataRepositoryPort;

    public SearchInstrumentsUseCaseImpl(
            InstrumentRepositoryPort instrumentRepositoryPort,
            MarketDataRepositoryPort marketDataRepositoryPort
    ) {
        this.instrumentRepositoryPort = instrumentRepositoryPort;
        this.marketDataRepositoryPort = marketDataRepositoryPort;
    }

    @Override
    public Page<InstrumentSearchResult> search(String query, int page, int limit) {
        Page<Instrument> instrumentsPage = instrumentRepositoryPort.search(query, page, limit);
        List<InstrumentSearchResult> content = instrumentsPage.content().stream()
                .map(this::enrichWithMarketData)
                .toList();
        return new Page<>(
                content,
                instrumentsPage.isFirst(),
                instrumentsPage.isLast(),
                instrumentsPage.totalElements(),
                instrumentsPage.totalPages()
        );
    }

    private InstrumentSearchResult enrichWithMarketData(Instrument instrument) {
        Optional<MarketData> marketDataOptional = marketDataRepositoryPort.findLatestByInstrumentId(instrument.id());
        BigDecimal lastPrice = marketDataOptional.map(MarketData::close).orElse(BigDecimal.ZERO);
        BigDecimal previousClose = marketDataOptional.map(MarketData::previousClose).orElse(BigDecimal.ZERO);
        BigDecimal variation = BigDecimal.ZERO;

        if (previousClose.compareTo(BigDecimal.ZERO) > 0) {
            variation = lastPrice.subtract(previousClose)
                    .divide(previousClose, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new InstrumentSearchResult(
                instrument,
                marketDataOptional.orElse(null),
                lastPrice,
                previousClose,
                variation
        );
    }
}
