package ar.com.cocos.challenge.infrastructure.database.specifications;

import ar.com.cocos.challenge.infrastructure.database.entities.MarketDataEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public final class MarketDataSpecifications {

    private MarketDataSpecifications() {}

    public static Specification<MarketDataEntity> latestForInstrumentIds(Collection<Integer> instrumentIds) {
        return (root, query, cb) -> {
            if (instrumentIds == null || instrumentIds.isEmpty()) {
                return cb.conjunction();
            }

            Subquery<Integer> subquery = query.subquery(Integer.class);
            Root<MarketDataEntity> subRoot = subquery.from(MarketDataEntity.class);
            subquery.select(cb.max(subRoot.get("id")));
            subquery.groupBy(subRoot.get("instrumentId"));
            subquery.where(subRoot.get("instrumentId").in(instrumentIds));

            return root.get("id").in(subquery);
        };
    }
}
