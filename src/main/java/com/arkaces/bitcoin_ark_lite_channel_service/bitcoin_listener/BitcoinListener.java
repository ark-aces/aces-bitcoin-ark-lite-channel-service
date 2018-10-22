package com.arkaces.bitcoin_ark_lite_channel_service.bitcoin_listener;

import com.arkaces.bitcoin_ark_lite_channel_service.contract.ContractEntity;
import com.arkaces.bitcoin_ark_lite_channel_service.contract.ContractRepository;
import com.arkaces.bitcoin_ark_lite_channel_service.electrum.ElectrumRpcClient;
import com.arkaces.bitcoin_ark_lite_channel_service.electrum.RpcResponse;
import com.arkaces.bitcoin_ark_lite_channel_service.transfer.BitcoinTransaction;
import com.arkaces.bitcoin_ark_lite_channel_service.transfer.NewBitcoinTransactionEvent;
import com.arkaces.bitcoin_ark_lite_channel_service.transfer.TransferEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BitcoinListener {

    private final ElectrumRpcClient electrumRpcClient;
    private final ContractRepository contractRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Integer bitcoinMinConfirmations;
    private final NetworkParameters bitcoinNetworkParameters;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${bitcoinScanIntervalSec}000")
    public void scan() {
        log.info("Scanning for new transactions");
        try {
            String blockHeaderResponseJson = electrumRpcClient
                    .sendCommand("blockchain.headers.subscribe", Collections.emptyList());
            RpcResponse<BlockHeaderResponse> rpcResponse = objectMapper.readValue(blockHeaderResponseJson,
                    new TypeReference<RpcResponse<BlockHeaderResponse>>(){});
            BlockHeaderResponse blockHeaderResponse = rpcResponse.getResult();
            Integer currentHeight = blockHeaderResponse.getBlockHeight();

            List<ContractEntity> contractEntities = contractRepository.findAll();
            for (ContractEntity contractEntity : contractEntities) {
                ECKey ecKey = DumpedPrivateKey.fromBase58(bitcoinNetworkParameters, contractEntity.getDepositBtcAddressPrivateKey()).getKey();
                Address senderAddress = ecKey.toAddress(bitcoinNetworkParameters);
                Script script = ScriptBuilder.createOutputScript(senderAddress);
                String scriptHashReversed = HexUtils.toHexString(Sha256Hash.of(script.getProgram()).getReversedBytes());

                String responseJson = electrumRpcClient
                        .sendCommand("blockchain.scripthash.get_history", Collections.singletonList(scriptHashReversed));

                List<HistoryResponseRow> historyResponseRows;
                try {
                    RpcResponse<List<HistoryResponseRow>> historyRpcResposne = objectMapper.readValue(responseJson,
                        new TypeReference<RpcResponse<List<HistoryResponseRow>>>() {});
                    historyResponseRows = historyRpcResposne.getResult();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to parse json response", e);
                }

                List<String> existingTxnIds = contractEntity.getTransferEntities().stream()
                        .map(TransferEntity::getBtcTransactionId)
                        .collect(Collectors.toList());

                List<String> newTxnIds = historyResponseRows.stream()
                        .filter(row -> !existingTxnIds.contains(row.getTxHash()))
                        .filter(row -> currentHeight - row.getHeight() > bitcoinMinConfirmations)
                        .map(HistoryResponseRow::getTxHash)
                        .collect(Collectors.toList());

                for (String txnId : newTxnIds) {
                    String txnResponseJson = electrumRpcClient
                            .sendCommand("blockchain.transaction.get", Collections.singletonList(txnId));

                    BitcoinTransaction bitcoinTransaction;
                    try {
                        RpcResponse<BitcoinTransaction> transactionRpcResponse =
                                objectMapper.readValue(txnResponseJson, new TypeReference<RpcResponse<BitcoinTransaction>>(){});
                        bitcoinTransaction = transactionRpcResponse.getResult();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to parse response json into bitcoin transaction: " + txnResponseJson, e);
                    }

                    NewBitcoinTransactionEvent newBitcoinTransactionEvent = new NewBitcoinTransactionEvent();
                    newBitcoinTransactionEvent.setContractPid(contractEntity.getPid());
                    newBitcoinTransactionEvent.setTransactionId(txnId);
                    newBitcoinTransactionEvent.setBitcoinTransaction(bitcoinTransaction);
                    applicationEventPublisher.publishEvent(newBitcoinTransactionEvent);
                }
            }
        } catch (Exception e) {
            log.error("Failed to scan blockchain", e);
        }
    }
}
