package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import lombok.Data;

import java.util.List;

/*
Example response:

{
  "asm": "OP_DUP OP_HASH160 997bf247fd054489330614f5a22f60a3ccfb3ad3 OP_EQUALVERIFY OP_CHECKSIG",
  "hex": "76a914997bf247fd054489330614f5a22f60a3ccfb3ad388ac",
  "reqSigs": 1,
  "type": "pubkeyhash",
  "addresses": [
    "muWWAMMKpKLb7toJrHscHXF91f87ZVkuNW"
  ]
}
 */
@Data
public class ElectrumTransactionScriptPubKey {
    private String hex;
    private String type;
    private List<String> addresses;
}
