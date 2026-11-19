package ar.com.cocos.challenge.interfaces.endpoint.controller;

import ar.com.cocos.challenge.application.inbound.GetPortfolioUseCase;
import ar.com.cocos.challenge.domain.model.Portfolio;
import ar.com.cocos.challenge.interfaces.endpoint.mapper.PortfolioInterfaceMapper;
import ar.com.cocos.challenge.interfaces.endpoint.resources.PortfolioResource;
import ar.com.cocos.challenge.interfaces.endpoint.response.PortfolioResponse;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioController implements PortfolioResource {

    private final GetPortfolioUseCase getPortfolioUseCase;
    private final PortfolioInterfaceMapper portfolioInterfaceMapper;

    public PortfolioController(
            GetPortfolioUseCase getPortfolioUseCase,
            PortfolioInterfaceMapper portfolioInterfaceMapper
    ) {
        this.getPortfolioUseCase = getPortfolioUseCase;
        this.portfolioInterfaceMapper = portfolioInterfaceMapper;
    }

    @Override
    public PortfolioResponse getPortfolio(Integer userId) {
        Portfolio portfolio = getPortfolioUseCase.getPortfolio(userId);
        return portfolioInterfaceMapper.toPortfolioResponse(portfolio);
    }
}
