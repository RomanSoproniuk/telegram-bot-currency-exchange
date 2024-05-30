package com.rsoproniuk.currencytelegrambot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsoproniuk.currencytelegrambot.dto.external.CurrencyResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyApiService {
    private final ObjectMapper objectMapper;
    private static final String CURRENCY_API_URL
            = "https://api.currencyapi.com/v3/latest?apikey=%s&currencies=%s&base_currency=%s";
    private static final String SEPARATOR = "%2C";
    @Value("${currency.api.key}")
    private String apiKey;

    public CurrencyResponseDto getCurrencyRate(
            String baseCurrency,
            List<String> currencies) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String url = CURRENCY_API_URL.formatted(apiKey, String.join(SEPARATOR, currencies), baseCurrency);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> send = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(send.body(), CurrencyResponseDto.class);
    }
}
