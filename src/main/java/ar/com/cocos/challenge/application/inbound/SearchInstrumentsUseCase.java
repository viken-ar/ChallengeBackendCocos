package ar.com.cocos.challenge.application.inbound;

import ar.com.cocos.challenge.domain.model.InstrumentSearchResult;
import ar.com.cocos.challenge.domain.model.Page;

public interface SearchInstrumentsUseCase {
    Page<InstrumentSearchResult> search(String query, int page, int limit);
}
