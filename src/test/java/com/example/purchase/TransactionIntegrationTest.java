package com.example.purchase;

import com.example.purchase.dto.CreateTransactionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full End-to-End Test Suite simulating realistic client utilization workflows.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0) // Configures WireMock to automatically claim an ephemeral port locally
public class TransactionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    public void testFullStoreAndConversionFlow() throws Exception {
        // Step 1: Prepare payload containing an un-rounded target value to verify scaling rules
        CreateTransactionRequest request = new CreateTransactionRequest(
                "Wireless Headphones", 
                LocalDate.of(2026, 4, 12), 
                new BigDecimal("99.984")
        );

        // Step 2: Post transaction model and evaluate base parameters mapping
        String postResponse = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.purchaseAmountUsd").value(99.98)) // Assert HALF_UP dropped the fractional half cent
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(postResponse).get("id").asText();

        // Step 3: Use WireMock to intercept the client request and return a controlled, mock JSON structure
        stubFor(get(urlPathEqualTo("/treasury_reporting_rates_exchange"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"data\": [{\"record_date\": \"2026-03-31\", \"exchange_rate\": \"1.25\"}]}")));

        // Step 4: Call the convert endpoint and verify the precision scaling of the conversion calculation
        mockMvc.perform(get("/api/transactions/" + id + "/convert")
                        .param("targetCurrency", "Euro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(124.98)) // 99.98 USD * 1.25 rate
                .andExpect(jsonPath("$.exchangeRate").value(1.25));
    }
}