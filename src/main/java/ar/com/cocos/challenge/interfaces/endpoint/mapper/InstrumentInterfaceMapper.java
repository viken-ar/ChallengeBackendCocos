package ar.com.cocos.challenge.interfaces.endpoint.mapper;

import ar.com.cocos.challenge.domain.model.InstrumentSearchResult;
import ar.com.cocos.challenge.interfaces.endpoint.response.InstrumentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstrumentInterfaceMapper {

    @Mapping(target = "id", source = "instrument.id")
    @Mapping(target = "ticker", source = "instrument.ticker")
    @Mapping(target = "name", source = "instrument.name")
    @Mapping(target = "type", source = "instrument.type")
    @Mapping(target = "lastPrice", source = "lastPrice")
    @Mapping(target = "previousClose", source = "previousClose")
    @Mapping(target = "dailyVariationPercentage", source = "dailyVariationPercentage")
    InstrumentResponse toResponse(InstrumentSearchResult domain);

    List<InstrumentResponse> toResponseList(List<InstrumentSearchResult> domains);
}
