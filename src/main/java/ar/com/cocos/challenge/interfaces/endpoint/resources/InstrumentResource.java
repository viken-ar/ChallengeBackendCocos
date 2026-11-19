package ar.com.cocos.challenge.interfaces.endpoint.resources;

import ar.com.cocos.challenge.interfaces.endpoint.response.InstrumentResponse;
import ar.com.cocos.challenge.interfaces.endpoint.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("")
@Tag(name = "Instruments", description = "Búsqueda y cotizaciones de instrumentos")
public interface InstrumentResource {

    @GetMapping("/v1/instruments")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Buscar activos por ticker y/o nombre")
    PageResponse<InstrumentResponse> searchInstruments(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    );
}
