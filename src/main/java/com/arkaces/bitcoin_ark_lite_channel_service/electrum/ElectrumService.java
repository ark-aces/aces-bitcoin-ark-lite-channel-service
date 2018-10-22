package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElectrumService {

    private static final BigInteger SATOSHIS_PER_BTC = new BigInteger("100000000");

    private final ElectrumRpcClient electrumRpcClient;
    private final NetworkParameters bitcoinNetworkParameters;

    private final ObjectMapper objectMapper = new ElectrumObjectMapperFactory().create();

    public String sendTransaction(String recipientBtcAddress, BigDecimal btcAmount, String senderPrivateKey) {
        ECKey ecKey = DumpedPrivateKey.fromBase58(bitcoinNetworkParameters, senderPrivateKey).getKey();
        Address senderAddress = ecKey.toAddress(bitcoinNetworkParameters);
        Script script = ScriptBuilder.createOutputScript(senderAddress);

        Transaction tx = new Transaction(bitcoinNetworkParameters);
        tx.setPurpose(Transaction.Purpose.USER_PAYMENT);

        long sendSatoshiAmount = btcAmount
                .multiply(new BigDecimal(SATOSHIS_PER_BTC))
                .toBigIntegerExact()
                .longValue();

        Coin sendAmount = Coin.valueOf(sendSatoshiAmount);
        Address recipientAddress = Address.fromBase58(bitcoinNetworkParameters, recipientBtcAddress);
        tx.addOutput(sendAmount, recipientAddress);

        // get unspent outputs
        String scriptHashReversed = HexUtils.toHexString(Sha256Hash.of(script.getProgram()).getReversedBytes());
        String unspentResponseJson = electrumRpcClient
                .sendCommand("blockchain.scripthash.listunspent", Collections.singletonList(scriptHashReversed));
        RpcResponse<List<UnspentOutput>> unspentResponse;
        try {
            unspentResponse = objectMapper.readValue(unspentResponseJson, new TypeReference<RpcResponse<List<UnspentOutput>>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse json response", e);
        }

        Long totalUnspentSatoshis = unspentResponse.getResult().stream()
                .mapToLong(UnspentOutput::getValue)
                .sum();
        Coin totalChange = Coin.valueOf(totalUnspentSatoshis)
                .subtract(sendAmount)
                .subtract(Transaction.DEFAULT_TX_FEE);

        // If total change is < 0 we should bail out now since the unspent outputs have enough value
        if (totalChange.compareTo(Coin.ZERO) < 0) {
            throw new RuntimeException("Insufficient funds to send transaction");
        }

        // Add change output if > 0 to sender address
        if (totalChange.compareTo(Coin.ZERO) > 0) {
            tx.addOutput(totalChange, senderAddress);
        }

        for (UnspentOutput unspentOutput : unspentResponse.getResult()) {
            Sha256Hash sha256Hash = Sha256Hash.wrap(HexUtils.fromHexString(unspentOutput.getTxHash()));
            TransactionOutPoint outPoint = new TransactionOutPoint(bitcoinNetworkParameters, unspentOutput.getTxPos(), sha256Hash);

            // Fetch transaction details so we can sign
            String transactionResponseJson = electrumRpcClient
                    .sendCommand("blockchain.transaction.get", Arrays.asList(unspentOutput.getTxHash(), true));

            RpcResponse<ElectrumTransaction> transactionResponse;
            try {
                transactionResponse = objectMapper.readValue(transactionResponseJson,
                        new TypeReference<RpcResponse<ElectrumTransaction>>(){});
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse json response", e);
            }

            // get script from corresponding transaction output from unspent transactions
            String scriptPubKeyHex = transactionResponse.getResult().getVout()
                    .get(unspentOutput.getTxPos())
                    .getScriptPubKey()
                    .getHex();
            byte[] outputScriptPubKey = HexUtils.fromHexString(scriptPubKeyHex);
            Script scriptPubKey = new Script(outputScriptPubKey);

            tx.addSignedInput(outPoint, scriptPubKey, ecKey, Transaction.SigHash.ALL, true);
        }

        String rawTxnData = HexUtils.toHexString(tx.bitcoinSerialize());

        String responseJson = electrumRpcClient
                .sendCommand("blockchain.transaction.broadcast", Collections.singletonList(rawTxnData));

        String transactionId;
        try {
            RpcResponse<String> response = objectMapper.readValue(responseJson, new TypeReference<RpcResponse<String>>(){});
            transactionId = response.getResult();
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse broadcast response: " + responseJson, e);
        }

        return transactionId;
    }

}
