package com.arkaces.bitcoin_ark_lite_channel_service.exchange_rate;

import com.arkaces.bitcoin_ark_lite_channel_service.ExchangeRateSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeRateService {

    private final ExchangeRateSettings exchangeRateSettings;

    private final RestTemplate restTemplate = new RestTemplateBuilder()
        .rootUri("https://min-api.cryptocompare.com/data/")
        .build();

    public BigDecimal getRate() {
        BigDecimal rate;
        if (exchangeRateSettings.getFixedRate() != null) {
            rate = exchangeRateSettings.getFixedRate();
        } else {
            // todo: we should cache this since it's does an external api call
            rate = getRateFromCryptoCompareApi();
        }

        return rate.multiply(exchangeRateSettings.getMultiplier());
    }

    private BigDecimal getRateFromCryptoCompareApi() {
        String fromCurrencyCode = exchangeRateSettings.getFromSymbol();
        String toCurrencyCode = exchangeRateSettings.getToSymbol();

        return restTemplate
                .exchange(
                        "/price?fsym={from}&tsyms={to}",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<Map<String, BigDecimal>>() {},
                        fromCurrencyCode,
                        toCurrencyCode
                )
                .getBody()
                .get(toCurrencyCode);
    }
}
