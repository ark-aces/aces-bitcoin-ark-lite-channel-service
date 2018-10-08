package com.arkaces.bitcoin_ark_lite_channel_service.bitcoin_listener;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/*
https://electrumx.readthedocs.io/en/latest/protocol-methods.html#blockchain-headers-subscribe

{
  "bits": 402858285,
  "block_height": 520481,
  "merkle_root": "8e8e932eb858fd53cf09943d7efc9a8f674dc1363010ee64907a292d2fb0c25d",
  "nonce": 3288656012,
  "prev_block_hash": "000000000000000000b512b5d9fc7c5746587268547c04aa92383aaea0080289",
  "timestamp": 1520495819,
  "version": 536870912
}
 */
@Data
public class BlockHeaderResponse {
    @JsonProperty("block_height")
    private Integer blockHeight;
}
