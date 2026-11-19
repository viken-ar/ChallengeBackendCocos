package ar.com.cocos.challenge.infrastructure.database.adapter;

import ar.com.cocos.challenge.application.outbound.MarketDataRepositoryPort;
import ar.com.cocos.challenge.domain.model.MarketData;
import ar.com.cocos.challenge.infrastructure.database.entities.MarketDataEntity;
import ar.com.cocos.challenge.infrastructure.database.mappers.MarketDataInfrastructureMapper;
import ar.com.cocos.challenge.infrastructure.database.repositories.MarketDataJpaRepository;
import ar.com.cocos.challenge.infrastructure.database.specifications.MarketDataSpecifications;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MarketDataRepositoryAdapter implements MarketDataRepositoryPort {

    private final MarketDataJpaRepository marketDataJpaRepository;
    private final MarketDataInfrastructureMapper marketDataInfrastructureMapper;

    public MarketDataRepositoryAdapter(
            MarketDataJpaRepository marketDataJpaRepository,
            MarketDataInfrastructureMapper marketDataInfrastructureMapper
    ) {
        this.marketDataJpaRepository = marketDataJpaRepository;
        this.marketDataInfrastructureMapper = marketDataInfrastructureMapper;
    }

    @Override
    public Optional<MarketData> findLatestByInstrumentId(Integer instrumentId) {
        return marketDataJpaRepository.findFirstByInstrumentIdOrderByDateDescIdDesc(instrumentId)
                .map(marketDataInfrastructureMapper::toDomain);
    }

    @Override
    public List<MarketData> findLatestForInstruments(List<Integer> instrumentIds) {
        if (instrumentIds == null || instrumentIds.isEmpty()) {
            return List.of();
        }
        List<MarketDataEntity> entities = marketDataJpaRepository.findAll(
            MarketDataSpecifications.latestForInstrumentIds(instrumentIds)
        );
        return marketDataInfrastructureMapper.toDomainList(entities);
    }

    @Override
    public MarketData save(MarketData marketData) {
        MarketDataEntity entity = marketDataInfrastructureMapper.toEntity(marketData);
        MarketDataEntity saved = marketDataJpaRepository.save(entity);
        return marketDataInfrastructureMapper.toDomain(saved);
    }
}
