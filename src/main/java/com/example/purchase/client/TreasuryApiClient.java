package com.example.purchase.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class TreasuryApiClient {

    private final RestClient restClient;

    /**
     * Initializes a modern Spring 3 RestClient using externalized configuration paths.
     */
    public TreasuryApiClient(@Value("${treasury.api.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Interrogates remote fiscal data API matching strict target criteria windows.
     */
    public List<ExchangeRateRecord> fetchExchangeRates(String currency, LocalDate startDate, LocalDate endDate) {
        // Smart Conversion: Constructs targeted logic queries for exact country currency filtering
        // and temporal framing requirements.
        String filter = String.format("currency:eq:%s,record_date:lte:%s,record_date:gte:%s", 
                currency, endDate, startDate);

        TreasuryApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/treasury_reporting_rates_exchange")
                        .queryParam("filter", filter)
                        .queryParam("sort", "-record_date") // Enforces descending order to capture freshest matches
                        .queryParam("page[size]", "1")      // Minimizes network load by limiting the response array size
                        .build())
                .retrieve()
                .body(TreasuryApiResponse.class);

        return response != null ? response.data() : List.of();
    }

    // Inner record definitions matching JSON object mappings returned by the Treasury API
    public record TreasuryApiResponse(List<ExchangeRateRecord> data) {}
    public record ExchangeRateRecord(
        @JsonProperty("record_date") LocalDate recordDate,
        @JsonProperty("exchange_rate") BigDecimal exchangeRate
    ) {}
}