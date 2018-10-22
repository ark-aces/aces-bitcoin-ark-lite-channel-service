package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import lombok.Data;

import java.math.BigDecimal;

/*
Example response:

  {
    "value": 2.0,
    "n": 0,
    "scriptPubKey": {
      "asm": "OP_DUP OP_HASH160 997bf247fd054489330614f5a22f60a3ccfb3ad3 OP_EQUALVERIFY OP_CHECKSIG",
      "hex": "76a914997bf247fd054489330614f5a22f60a3ccfb3ad388ac",
      "reqSigs": 1,
      "type": "pubkeyhash",
      "addresses": [
        "muWWAMMKpKLb7toJrHscHXF91f87ZVkuNW"
      ]
    }
  }
 */
@Data
public class ElectrumTransactionVout {

    private BigDecimal value;
    private Integer n;
    private ElectrumTransactionScriptPubKey scriptPubKey;

}
