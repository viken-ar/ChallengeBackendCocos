package ar.com.cocos.challenge.infrastructure.database.specifications;

import ar.com.cocos.challenge.infrastructure.database.entities.InstrumentEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public final class InstrumentSpecifications {

    private InstrumentSpecifications() {}

    public static Specification<InstrumentEntity> searchByQuery(String query) {
        return (root, queryCriteria, cb) -> {
            if (StringUtils.isBlank(query)) {
                return cb.conjunction();
            }
            String pattern = "%" + query.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("ticker")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern)
            );
        };
    }
}
