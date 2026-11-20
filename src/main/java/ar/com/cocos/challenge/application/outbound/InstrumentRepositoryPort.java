package ar.com.cocos.challenge.application.outbound;

import ar.com.cocos.challenge.domain.model.Instrument;
import ar.com.cocos.challenge.domain.model.InstrumentType;
import ar.com.cocos.challenge.domain.model.Page;
import java.util.List;
import java.util.Optional;

public interface InstrumentRepositoryPort {
    Optional<Instrument> findById(Integer id);
    Optional<Instrument> findByTicker(String ticker);
    Optional<Instrument> findByType(InstrumentType type);
    Page<Instrument> search(String query, int page, int limit);
    List<Instrument> findAll();
    Instrument save(Instrument instrument);
}
