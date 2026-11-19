package ar.com.cocos.challenge.infrastructure.database.repositories;

import ar.com.cocos.challenge.infrastructure.database.entities.MarketDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MarketDataJpaRepository extends JpaRepository<MarketDataEntity, Integer>, JpaSpecificationExecutor<MarketDataEntity> {

    Optional<MarketDataEntity> findFirstByInstrumentIdOrderByDateDescIdDesc(Integer instrumentId);
}
