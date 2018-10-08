package com.arkaces.bitcoin_ark_lite_channel_service.bitcoin_listener;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/*
Response from https://electrumx.readthedocs.io/en/latest/protocol-methods.html#blockchain-scripthash-get-history
{
    "height": 200004,
    "tx_hash": "acc3758bd2a26f869fcc67d48ff30b96464d476bca82c1cd6656e7d506816412"
},
 */
@Data
public class HistoryResponseRow {
    private Integer height;

    @JsonProperty("tx_hash")
    private String txHash;
}
