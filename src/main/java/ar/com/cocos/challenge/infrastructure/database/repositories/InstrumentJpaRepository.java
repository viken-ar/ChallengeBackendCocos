package ar.com.cocos.challenge.infrastructure.database.repositories;

import ar.com.cocos.challenge.infrastructure.database.entities.InstrumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InstrumentJpaRepository extends JpaRepository<InstrumentEntity, Integer>, JpaSpecificationExecutor<InstrumentEntity> {
    Optional<InstrumentEntity> findByTicker(String ticker);
}
