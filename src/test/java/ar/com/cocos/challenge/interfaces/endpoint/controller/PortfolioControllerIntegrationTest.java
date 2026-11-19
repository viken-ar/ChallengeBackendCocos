package ar.com.cocos.challenge.interfaces.endpoint.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class PortfolioControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getPortfolio_forExistingUser1_shouldReturnCorrectValuationAndHoldings() throws Exception {
        mockMvc.perform(get("/v1/users/1/portfolio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(1)))
                .andExpect(jsonPath("$.accountNumber", is("10001")))
                .andExpect(jsonPath("$.availableCash", is(753000.0)))
                .andExpect(jsonPath("$.totalAccountValue", notNullValue()))
                .andExpect(jsonPath("$.positions", hasSize(2)))
                .andExpect(jsonPath("$.positions[*].ticker", containsInAnyOrder("PAMP", "METR")));
    }
}
