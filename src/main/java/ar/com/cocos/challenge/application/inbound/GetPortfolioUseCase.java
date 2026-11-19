package ar.com.cocos.challenge.application.inbound;

import ar.com.cocos.challenge.domain.model.Portfolio;

public interface GetPortfolioUseCase {
    Portfolio getPortfolio(Integer userId);
}
