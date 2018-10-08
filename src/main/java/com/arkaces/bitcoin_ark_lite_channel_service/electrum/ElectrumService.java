package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElectrumService {

    private final ElectrumRpcClient electrumRpcClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String sendTransaction(String btcAddress, BigDecimal btcAmount) {
//        Transaction transaction = new Transaction(MainNetParams.get());
//        transaction.addOutput(coin1,address1);
//        transaction.addOutput(coin2,address2);
//        SendRequest req = SendRequest.forTx(transaction);
        String rawTxnData = ""; // todo generate raw transaction

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
