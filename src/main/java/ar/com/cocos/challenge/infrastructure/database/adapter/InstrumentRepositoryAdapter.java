package ar.com.cocos.challenge.infrastructure.database.adapter;

import ar.com.cocos.challenge.application.outbound.InstrumentRepositoryPort;
import ar.com.cocos.challenge.domain.model.Instrument;
import ar.com.cocos.challenge.domain.model.InstrumentType;
import ar.com.cocos.challenge.domain.model.Page;
import ar.com.cocos.challenge.infrastructure.database.entities.InstrumentEntity;
import ar.com.cocos.challenge.infrastructure.database.mappers.InstrumentInfrastructureMapper;
import ar.com.cocos.challenge.infrastructure.database.repositories.InstrumentJpaRepository;
import ar.com.cocos.challenge.infrastructure.database.specifications.InstrumentSpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class InstrumentRepositoryAdapter implements InstrumentRepositoryPort {

    private final InstrumentJpaRepository instrumentJpaRepository;
    private final InstrumentInfrastructureMapper instrumentInfrastructureMapper;

    public InstrumentRepositoryAdapter(
            InstrumentJpaRepository instrumentJpaRepository,
            InstrumentInfrastructureMapper instrumentInfrastructureMapper
    ) {
        this.instrumentJpaRepository = instrumentJpaRepository;
        this.instrumentInfrastructureMapper = instrumentInfrastructureMapper;
    }

    @Override
    public Optional<Instrument> findById(Integer id) {
        return instrumentJpaRepository.findById(id).map(instrumentInfrastructureMapper::toDomain);
    }

    @Override
    public Optional<Instrument> findByTicker(String ticker) {
        return instrumentJpaRepository.findByTicker(ticker).map(instrumentInfrastructureMapper::toDomain);
    }

    @Override
    public Optional<Instrument> findByType(InstrumentType type) {
        return instrumentJpaRepository.findByType(type).map(instrumentInfrastructureMapper::toDomain);
    }

    @Override
    public Page<Instrument> search(String query, int page, int limit) {
        int zeroBasedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(zeroBasedPage, limit);
        Specification<InstrumentEntity> spec = InstrumentSpecifications.searchByQuery(query);
        var springPage = instrumentJpaRepository.findAll(spec, pageable);
        List<Instrument> content = instrumentInfrastructureMapper.toDomainList(springPage.getContent());
        return new Page<>(
                content,
                springPage.isFirst(),
                springPage.isLast(),
                springPage.getTotalElements(),
                springPage.getTotalPages()
        );
    }

    @Override
    public List<Instrument> findAll() {
        return instrumentInfrastructureMapper.toDomainList(instrumentJpaRepository.findAll());
    }

    @Override
    public Instrument save(Instrument instrument) {
        InstrumentEntity entity = instrumentInfrastructureMapper.toEntity(instrument);
        InstrumentEntity saved = instrumentJpaRepository.save(entity);
        return instrumentInfrastructureMapper.toDomain(saved);
    }
}
