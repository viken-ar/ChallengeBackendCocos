package ar.com.cocos.challenge.interfaces.endpoint.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class OrderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createOrder_marketBuy_withFunds_shouldBeFilled() throws Exception {
        UUID key = UUID.randomUUID();
        // User 1 has 753,000 ARS cash. Buy 10 shares of PAMP (47)
        String requestJson = """
            {
                "userId": 1,
                "instrumentId": 47,
                "side": "BUY",
                "type": "MARKET",
                "size": 10
            }
        """;

        mockMvc.perform(post("/v1/orders")
                        .header("Idempotency-Key", key.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.status", is("FILLED")))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.ticker", is("PAMP")));
    }

    @Test
    void createOrder_idempotency_shouldReturnOriginalResponseOnDuplicate() throws Exception {
        UUID key = UUID.randomUUID();
        String requestJson = """
            {
                "userId": 1,
                "instrumentId": 47,
                "side": "BUY",
                "type": "MARKET",
                "size": 2
            }
        """;

        // First call
        String firstResponse = mockMvc.perform(post("/v1/orders")
                        .header("Idempotency-Key", key.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        // Second call with same UUID
        String secondResponse = mockMvc.perform(post("/v1/orders")
                        .header("Idempotency-Key", key.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(firstResponse).isEqualTo(secondResponse);
    }

    @Test
    void createOrder_limitBuy_shouldBeNew() throws Exception {
        UUID key = UUID.randomUUID();
        String requestJson = """
            {
                "userId": 1,
                "instrumentId": 47,
                "side": "BUY",
                "type": "LIMIT",
                "size": 5,
                "price": 850.00
            }
        """;

        mockMvc.perform(post("/v1/orders")
                        .header("Idempotency-Key", key.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("NEW")))
                .andExpect(jsonPath("$.price", is(850.00)));
    }

    @Test
    void createOrder_missingIdempotencyHeader_shouldReturn400BadRequest() throws Exception {
        String requestJson = """
            {
                "userId": 1,
                "instrumentId": 47,
                "side": "BUY",
                "type": "MARKET",
                "size": 1
            }
        """;

        mockMvc.perform(post("/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Missing Request Header")));
    }

    @Test
    void cancelOrder_whenOrderIsNew_shouldReturn200Cancelled() throws Exception {
        // Order 5 is in status NEW in seed database
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/v1/orders/5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    void cancelOrder_whenOrderIsFilled_shouldReturn400BadRequest() throws Exception {
        // Order 1 is FILLED in seed database
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/v1/orders/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")));
    }
}
