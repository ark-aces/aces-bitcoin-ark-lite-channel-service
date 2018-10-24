package com.arkaces.bitcoin_ark_lite_channel_service.exchange_rate;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRate {
    private BigDecimal rate;
    private String from;
    private String to;
}
