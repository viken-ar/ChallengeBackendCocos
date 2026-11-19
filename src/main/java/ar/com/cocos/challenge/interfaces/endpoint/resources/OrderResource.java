package ar.com.cocos.challenge.interfaces.endpoint.resources;

import ar.com.cocos.challenge.interfaces.endpoint.request.CreateOrderRequest;
import ar.com.cocos.challenge.interfaces.endpoint.response.OrderResponse;
import ar.com.cocos.challenge.interfaces.endpoint.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("")
public interface OrderResource {

    @PostMapping("/v1/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enviar una orden de compra o venta al mercado (Idempotente)")
    @Tag(name = "Orders", description = "Endpoints para gestión de órdenes bursátiles")
    OrderResponse createOrder(
        @Parameter(in = ParameterIn.HEADER, name = "Idempotency-Key", description = "Clave de idempotencia UUID", required = true)
        @RequestHeader("Idempotency-Key") UUID idempotencyKey,
        @Valid @RequestBody CreateOrderRequest request
    );

    @DeleteMapping("/v1/orders/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancelar una orden pendiente en estado NEW")
    @Tag(name = "Orders", description = "Endpoints para gestión de órdenes bursátiles")
    OrderResponse cancelOrder(@PathVariable Integer orderId);

    @GetMapping("/v1/users/{userId}/orders")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtener todas las órdenes de un usuario (paginable)")
    @Tag(name = "Users", description = "Endpoints para gestión de órdenes bursátiles")
    PageResponse<OrderResponse> getUserOrders(
            @PathVariable Integer userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    );

    @GetMapping("/v1/users/{userId}/orders/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Obtener una orden específica de un usuario")
    @Tag(name = "Users", description = "Endpoints para gestión de órdenes bursátiles")
    OrderResponse getUserOrderById(@PathVariable Integer userId, @PathVariable Integer orderId);
}
