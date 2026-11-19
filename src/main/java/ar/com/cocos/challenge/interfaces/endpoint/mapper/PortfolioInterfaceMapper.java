package ar.com.cocos.challenge.interfaces.endpoint.mapper;

import ar.com.cocos.challenge.domain.model.Portfolio;
import ar.com.cocos.challenge.domain.model.Position;
import ar.com.cocos.challenge.interfaces.endpoint.response.PortfolioResponse;
import ar.com.cocos.challenge.interfaces.endpoint.response.PositionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PortfolioInterfaceMapper {

    @Mapping(target = "size", source = "sharesCount")
    @Mapping(target = "totalMonetaryValue", source = "totalValue")
    PositionResponse toPositionResponse(Position position);

    List<PositionResponse> toPositionResponseList(List<Position> positions);

    PortfolioResponse toPortfolioResponse(Portfolio portfolio);
}
