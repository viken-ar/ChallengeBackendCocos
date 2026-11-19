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
class InstrumentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void searchInstruments_byTicker_shouldReturnMatchingInstrument() throws Exception {
        mockMvc.perform(get("/v1/instruments")
                        .param("query", "YPFD")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].ticker", is("YPFD")))
                .andExpect(jsonPath("$.content[0].name", containsString("Y.P.F.")))
                .andExpect(jsonPath("$.content[0].lastPrice", notNullValue()))
                .andExpect(jsonPath("$.isFirst", is(true)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void searchInstruments_byNamePartial_shouldReturnMatches() throws Exception {
        mockMvc.perform(get("/v1/instruments")
                        .param("query", "galicia")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].ticker", is("GGAL")));
    }

    @Test
    void searchInstruments_withNoQuery_shouldReturnAllInstruments() throws Exception {
        mockMvc.perform(get("/v1/instruments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(50)))
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }
}
