package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import lombok.Data;

import java.util.List;

/*
Example response:

{
  "jsonrpc": "2.0",
  "result": {
    "txid": "85b497a6b791ea98041042a90f2980cc7957d6d8dcb2ebeb83d12f2ddcf27fa4",
    "hash": "992352020e662da2d7b07c6df4b2382b05cba056b23788040dcbb5b20defc593",
    "version": 1,
    "size": 250,
    "vsize": 168,
    "weight": 670,
    "locktime": 0,
    "vin": [
      {
        "txid": "e9dcfb4c68e79a76f036c63fb60991565a63e8bab5d8c007ee33ed7bb4dd7824",
        "vout": 1,
        "scriptSig": {
          "asm": "0014ed002c33d3340a49407d6431cee5c69fa7da2c9b",
          "hex": "160014ed002c33d3340a49407d6431cee5c69fa7da2c9b"
        },
        "txinwitness": [
          "3045022100a4580302e975cbf09a9230597868f5e9788304a9982ffbf23924b0fc32bb7ea602203d19df7945cb711fa2e267b1f12f53fc7100450f8d1d9194004a55f227545eee01",
          "030cb5bda15c17b72eae1c1fa2229cd31b794c00fe642a8538a70f6e3427f57b97"
        ],
        "sequence": 4294967295
      }
    ],
    "vout": [
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
      },
      {
        "value": 9906.18948255,
        "n": 1,
        "scriptPubKey": {
          "asm": "OP_HASH160 1ad08e42e5ece151c35f5e82652c5beab3c05fdd OP_EQUAL",
          "hex": "a9141ad08e42e5ece151c35f5e82652c5beab3c05fdd87",
          "reqSigs": 1,
          "type": "scripthash",
          "addresses": [
            "2Muh1PyaApCrzvwX6cz6xFC3Df5Ct56d8wc"
          ]
        }
      }
    ],
    "hex": "010000000001012478ddb47bed33ee07c0d8b5bae8635a569109b63fc636f0769ae7684cfbdce90100000017160014ed002c33d3340a49407d6431cee5c69fa7da2c9bffffffff0200c2eb0b000000001976a914997bf247fd054489330614f5a22f60a3ccfb3ad388ac9f927da5e600000017a9141ad08e42e5ece151c35f5e82652c5beab3c05fdd8702483045022100a4580302e975cbf09a9230597868f5e9788304a9982ffbf23924b0fc32bb7ea602203d19df7945cb711fa2e267b1f12f53fc7100450f8d1d9194004a55f227545eee0121030cb5bda15c17b72eae1c1fa2229cd31b794c00fe642a8538a70f6e3427f57b9700000000",
    "blockhash": "000000000000072ddc84fa6a342eedee7c4345d679678073ebada9cc19236da1",
    "confirmations": 179501,
    "time": 1516828226,
    "blocktime": 1516828226
  },
  "id": "c468c186-4a87-4442-a83a-4b04d060eded"
}
 */
@Data
public class ElectrumTransaction {

    private String txid;
    private String hash;
    private Integer version;
    private Integer size;
    private Integer vsize;
    private Integer weight;
    private Integer locktime;
    private List<ElectrumTransactionVout> vout;
    private String hex;
    private String blockhash;
    private Integer confirmations;
    private Long time;
    private Long blocktime;

}
