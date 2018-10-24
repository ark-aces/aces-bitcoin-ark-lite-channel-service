package com.arkaces.bitcoin_ark_lite_channel_service.bitcoin_listener;

import lombok.Data;

/*
https://electrumx.readthedocs.io/en/latest/protocol-methods.html#blockchain-headers-subscribe

{
  "height": 520481,
  "hex": "00000020890208a0ae3a3892aa047c5468725846577cfcd9b512b50000000000000000005dc2b02f2d297a9064ee103036c14d678f9afc7e3d9409cf53fd58b82e938e8ecbeca05a2d2103188ce804c4"
}
 */
@Data
public class BlockHeaderResponse {
    private Integer height;
    private String hex;
}
