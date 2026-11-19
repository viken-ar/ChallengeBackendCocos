package ar.com.cocos.challenge.application.outbound;

import ar.com.cocos.challenge.domain.model.MarketData;
import java.util.List;
import java.util.Optional;

public interface MarketDataRepositoryPort {
    Optional<MarketData> findLatestByInstrumentId(Integer instrumentId);
    List<MarketData> findLatestForInstruments(List<Integer> instrumentIds);
    MarketData save(MarketData marketData);
}
