package com.arkaces.bitcoin_ark_lite_channel_service.transfer;

import lombok.Data;

@Data
public class NewBitcoinTransactionEvent {
    private Long contractPid;
    private String transactionId;
    private BitcoinTransaction bitcoinTransaction;
}

