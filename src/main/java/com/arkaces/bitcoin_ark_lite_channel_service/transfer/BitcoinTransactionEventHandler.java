package com.arkaces.bitcoin_ark_lite_channel_service.transfer;

import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import com.arkaces.bitcoin_ark_lite_channel_service.Constants;
import com.arkaces.bitcoin_ark_lite_channel_service.Config;
import com.arkaces.bitcoin_ark_lite_channel_service.contract.ContractEntity;
import com.arkaces.bitcoin_ark_lite_channel_service.contract.ContractRepository;
import com.arkaces.bitcoin_ark_lite_channel_service.exchange_rate.ExchangeRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BitcoinTransactionEventHandler {

    private final ContractRepository contractRepository;
    private final TransferRepository transferRepository;
    private final IdentifierGenerator identifierGenerator;
    private final ExchangeRateService exchangeRateService;
    private final Config config;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    public void handleBitcoinEvent(NewBitcoinTransactionEvent eventPayload) {
        String btcTransactionId = eventPayload.getTransactionId();
        
        log.info("Received Bitcoin event: " + btcTransactionId + " -> " + eventPayload.getBitcoinTransaction());
        
        ContractEntity contractEntity = contractRepository.findById(eventPayload.getContractPid()).orElse(null);
        if (contractEntity == null) {
            log.info("Bitcoin event has no corresponding contract: " + eventPayload);
            return;
        }
        
        log.info("Matched event for contract id " + contractEntity.getId() + " btc transaction id " + btcTransactionId);

        TransferEntity existingTransferEntity = transferRepository.findOneByBtcTransactionId(btcTransactionId);
        if (existingTransferEntity != null) {
            log.info("Transfer for btc transaction " + btcTransactionId + " already exists with id " + existingTransferEntity.getId());
            return;
        } 
        
        String transferId = identifierGenerator.generate();

        TransferEntity transferEntity = new TransferEntity();
        transferEntity.setId(transferId);
        transferEntity.setStatus(TransferStatus.NEW);
        transferEntity.setCreatedAt(LocalDateTime.now());
        transferEntity.setBtcTransactionId(btcTransactionId);
        transferEntity.setContractEntity(contractEntity);

        // Get BTC amount from transaction
        BitcoinTransaction bitcoinTransaction = eventPayload.getBitcoinTransaction();

        BigDecimal incomingBtcAmount = BigDecimal.ZERO;
        for (BitcoinTransactionVout vout : bitcoinTransaction.getVout()) {
            for (String address : vout.getScriptPubKey().getAddresses()) {
                if (address.equals(contractEntity.getDepositBtcAddress())) {
                    incomingBtcAmount = incomingBtcAmount.add(vout.getValue());
                }
            }
        }
        transferEntity.setBtcAmount(incomingBtcAmount);

        BigDecimal btcToArkRate = exchangeRateService.getRate("BTC", "ARK"); //2027.58, Ark 8, Btc 15000
        transferEntity.setBtcToArkRate(btcToArkRate);
        
        transferEntity.setBtcFlatFee(config.getFlatFee());
        transferEntity.setBtcPercentFee(config.getPercentFee());

        BigDecimal percentFee = config.getPercentFee()
                .divide(new BigDecimal("100.00"), 8, BigDecimal.ROUND_HALF_UP);
        BigDecimal btcTotalFeeAmount = incomingBtcAmount.multiply(percentFee).add(config.getFlatFee());
        transferEntity.setBtcTotalFee(btcTotalFeeAmount);

        // Calculate send ark amount
        BigDecimal btcSendAmount = incomingBtcAmount.subtract(btcTotalFeeAmount);
        BigDecimal arkSendAmount = btcSendAmount.multiply(btcToArkRate).setScale(8, RoundingMode.HALF_DOWN);
        if (arkSendAmount.compareTo(Constants.ARK_TRANSACTION_FEE) <= 0) {
            arkSendAmount = BigDecimal.ZERO;
        }
        transferEntity.setArkSendAmount(arkSendAmount);

        transferRepository.save(transferEntity);
        
        NewTransferEvent newTransferEvent = new NewTransferEvent();
        newTransferEvent.setTransferPid(transferEntity.getPid());
        applicationEventPublisher.publishEvent(newTransferEvent);
    }
}
