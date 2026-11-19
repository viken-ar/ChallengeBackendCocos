package ar.com.cocos.challenge.interfaces.endpoint.resources;

import ar.com.cocos.challenge.interfaces.endpoint.response.PortfolioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("")
@Tag(name = "Users", description = "Consulta de tenencias y saldos")
public interface PortfolioResource {

    @GetMapping("/v1/users/{userId}/portfolio")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtener el portfolio valorizado de un usuario")
    PortfolioResponse getPortfolio(@PathVariable Integer userId);
}
