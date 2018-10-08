package com.arkaces.bitcoin_ark_lite_channel_service.contract;

import lombok.Data;

@Data
public class Arguments {
    private String recipientArkAddress;
    private String returnBtcAddress;
}
