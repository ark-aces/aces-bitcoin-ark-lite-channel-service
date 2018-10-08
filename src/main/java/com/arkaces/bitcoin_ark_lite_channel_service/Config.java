package com.arkaces.bitcoin_ark_lite_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
@ConfigurationProperties
public class Config {
    private String capacityUnit;

    private BigDecimal flatFee;
    private String flatFeeUnit;
    private BigDecimal percentFee;

    private Integer bitcoinMinConfirmations;
}
