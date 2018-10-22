package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/*
Example response:

{
  "jsonrpc": "2.0",
  "result": [
    {
      "tx_hash": "85b497a6b791ea98041042a90f2980cc7957d6d8dcb2ebeb83d12f2ddcf27fa4",
      "tx_pos": 0,
      "height": 1260311,
      "value": 200000000
    },
    {
      "tx_hash": "bde5efdf2a273fcfb7d0e373550931719566baea9f72481a02f5d6367a1c11a1",
      "tx_pos": 0,
      "height": 1260338,
      "value": 200000000
    }
  ],
  "id": "20e8f88f-fb62-4467-8b16-f9b5a062674d"
}
 */
@Data
public class UnspentOutput {

    @JsonProperty("tx_hash")
    private String txHash;

    @JsonProperty("tx_pos")
    private Integer txPos;

    private Long height;

    private Long value;

}
