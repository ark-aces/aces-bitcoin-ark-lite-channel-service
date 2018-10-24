package com.arkaces.bitcoin_ark_lite_channel_service.exchange_rate;

import com.arkaces.bitcoin_ark_lite_channel_service.ExchangeRateSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExchangeRateController {

    private final ExchangeRateSettings exchangeRateSettings;
    private final ExchangeRateService exchangeRateService;

    @GetMapping("/exchangeRate")
    public ExchangeRate getExchangeRate() {
        String fromSymbol = exchangeRateSettings.getFromSymbol();
        String toSymbol = exchangeRateSettings.getToSymbol();

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setFrom(fromSymbol);
        exchangeRate.setTo(toSymbol);

        BigDecimal rate;
        if (exchangeRateSettings.getFixedRate() != null) {
            rate = exchangeRateSettings.getFixedRate();
        } else {
            // todo: we should cache this since it's does an external api call
            rate = exchangeRateService.getRate(fromSymbol, toSymbol);
        }

        BigDecimal adjustedRate = rate.multiply(exchangeRateSettings.getMultiplier());
        exchangeRate.setRate(adjustedRate);

        return exchangeRate;
    }
}
