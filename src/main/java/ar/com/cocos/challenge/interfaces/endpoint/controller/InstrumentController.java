package ar.com.cocos.challenge.interfaces.endpoint.controller;

import ar.com.cocos.challenge.application.inbound.SearchInstrumentsUseCase;
import ar.com.cocos.challenge.domain.model.InstrumentSearchResult;
import ar.com.cocos.challenge.domain.model.Page;
import ar.com.cocos.challenge.interfaces.endpoint.mapper.InstrumentInterfaceMapper;
import ar.com.cocos.challenge.interfaces.endpoint.resources.InstrumentResource;
import ar.com.cocos.challenge.interfaces.endpoint.response.InstrumentResponse;
import ar.com.cocos.challenge.interfaces.endpoint.response.PageResponse;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InstrumentController implements InstrumentResource {

    private final SearchInstrumentsUseCase searchInstrumentsUseCase;
    private final InstrumentInterfaceMapper instrumentInterfaceMapper;

    public InstrumentController(
            SearchInstrumentsUseCase searchInstrumentsUseCase,
            InstrumentInterfaceMapper instrumentInterfaceMapper
    ) {
        this.searchInstrumentsUseCase = searchInstrumentsUseCase;
        this.instrumentInterfaceMapper = instrumentInterfaceMapper;
    }

    @Override
    public PageResponse<InstrumentResponse> searchInstruments(String query, int page, int limit) {
        Page<InstrumentSearchResult> resultsPage = searchInstrumentsUseCase.search(query, page, limit);
        List<InstrumentResponse> content = instrumentInterfaceMapper.toResponseList(resultsPage.content());
        return new PageResponse<>(
                content,
                resultsPage.isFirst(),
                resultsPage.isLast(),
                resultsPage.totalElements(),
                resultsPage.totalPages()
        );
    }
}
