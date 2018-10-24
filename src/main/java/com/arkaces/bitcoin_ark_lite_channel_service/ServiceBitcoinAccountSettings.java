package com.arkaces.bitcoin_ark_lite_channel_service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "service-bitcoin-account")
public class ServiceBitcoinAccountSettings {
    private String privateKey;
}
