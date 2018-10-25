package com.arkaces.bitcoin_ark_lite_channel_service.transfer;

import com.arkaces.aces_server.aces_service.notification.NotificationService;
import com.arkaces.bitcoin_ark_lite_channel_service.Constants;
import com.arkaces.bitcoin_ark_lite_channel_service.ServiceBitcoinAccountSettings;
import com.arkaces.bitcoin_ark_lite_channel_service.ark.ArkService;
import com.arkaces.bitcoin_ark_lite_channel_service.contract.ContractEntity;
import com.arkaces.bitcoin_ark_lite_channel_service.electrum.ElectrumRpcClient;
import com.arkaces.bitcoin_ark_lite_channel_service.electrum.ElectrumService;
import com.arkaces.bitcoin_ark_lite_channel_service.service_capacity.ServiceCapacityEntity;
import com.arkaces.bitcoin_ark_lite_channel_service.service_capacity.ServiceCapacityRepository;
import com.arkaces.bitcoin_ark_lite_channel_service.service_capacity.ServiceCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final ArkService arkService;
    private final ElectrumService electrumService;
    private final ServiceCapacityService serviceCapacityService;
    private final ServiceCapacityRepository serviceCapacityRepository;
    private final ServiceBitcoinAccountSettings serviceBitcoinAccountSettings;
    private final NotificationService notificationService;
    private final BigDecimal lowCapacityThreshold;

    /**
     * @return true if amount reserved successfully
     */
    public boolean reserveTransferCapacity(Long transferPid) {
        // Lock service capacity and update available balance if available
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        BigDecimal totalAmount = transferEntity.getArkSendAmount().add(Constants.ARK_TRANSACTION_FEE);
        BigDecimal newAvailableAmount = serviceCapacityEntity.getAvailableAmount().subtract(totalAmount);
        BigDecimal newUnsettledAmount = serviceCapacityEntity.getUnsettledAmount().add(totalAmount);
        if (newAvailableAmount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        serviceCapacityEntity.setAvailableAmount(newAvailableAmount);
        serviceCapacityEntity.setUnsettledAmount(newUnsettledAmount);
        serviceCapacityRepository.save(serviceCapacityEntity);

        if (serviceCapacityEntity.getAvailableAmount().compareTo(lowCapacityThreshold) <= 0) {
            notificationService.notifyLowCapacity(serviceCapacityEntity.getAvailableAmount(), serviceCapacityEntity.getUnit());
        }
        
        return true;
    }
    
    public void settleTransferCapacity(Long transferPid) {
        ServiceCapacityEntity serviceCapacityEntity = serviceCapacityService.getLockedCapacityEntity();

        TransferEntity transferEntity = transferRepository.findById(transferPid)
                .orElseThrow(() -> new RuntimeException("Failed to get transfer with id " + transferPid));
        BigDecimal totalAmount = transferEntity.getArkSendAmount().add(Constants.ARK_TRANSACTION_FEE);

        serviceCapacityEntity.setUnsettledAmount(serviceCapacityEntity.getUnsettledAmount().subtract(totalAmount));
        serviceCapacityEntity.setTotalAmount(serviceCapacityEntity.getTotalAmount().subtract(totalAmount));

        serviceCapacityRepository.save(serviceCapacityEntity);
    }
    
    public void processNewTransfer(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        ContractEntity contractEntity = transferEntity.getContractEntity();

        BigDecimal totalAmount = transferEntity.getArkSendAmount().add(Constants.ARK_TRANSACTION_FEE);
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal arkSendAmount = transferEntity.getArkSendAmount();
            String recipientArkAddress = contractEntity.getRecipientArkAddress();
            String arkTransactionId = arkService.sendTransaction(recipientArkAddress, arkSendAmount);
            transferEntity.setArkTransactionId(arkTransactionId);

            log.info("Sent " + arkSendAmount + " ark to " + contractEntity.getRecipientArkAddress()
                + ", ark transaction id " + arkTransactionId + ", btc transaction " + transferEntity.getBtcTransactionId());
        } 
        
        transferEntity.setStatus(TransferStatus.COMPLETE);
        transferRepository.save(transferEntity);

        log.info("Saved transfer id " + transferEntity.getId() + " to contract " + contractEntity.getId());

        notificationService.notifySuccessfulTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId()
        );
    }

    /**
     * Process a full return due to insufficient capacity
     * @param transferPid
     */
    public void processReturn(Long transferPid) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);

        log.info("Insufficient ark to send transfer id = " + transferEntity.getId());

        String returnBtcAddress = transferEntity.getContractEntity().getReturnBtcAddress();
        if (returnBtcAddress != null) {
            BigDecimal bitcoinFee = new BigDecimal(Transaction.DEFAULT_TX_FEE.longValue())
                .divide(new BigDecimal(ElectrumService.SATOSHIS_PER_BTC), 10, BigDecimal.ROUND_HALF_UP);
            BigDecimal returnBtcAmount = transferEntity.getBtcAmount().subtract(bitcoinFee);
            String returnBtcTransactionId = electrumService.sendTransaction(
                    returnBtcAddress,
                    returnBtcAmount,
                    serviceBitcoinAccountSettings.getPrivateKey()
            );
            transferEntity.setStatus(TransferStatus.RETURNED);
            transferEntity.setReturnBtcTransactionId(returnBtcTransactionId);
        } else {
            log.warn("Bitcoin return could not be processed for transfer " + transferPid);
            transferEntity.setStatus(TransferStatus.FAILED);
        }

        transferRepository.save(transferEntity);

        notificationService.notifyFailedTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId(),
                "Insufficient ark to send transfer id = " + transferEntity.getId()
        );
    }
    
    public void processFailedTransfer(Long transferPid, String reasonMessage) {
        TransferEntity transferEntity = transferRepository.findOneForUpdate(transferPid);
        transferEntity.setStatus(TransferStatus.FAILED);
        transferRepository.save(transferEntity);

        notificationService.notifyFailedTransfer(
                transferEntity.getContractEntity().getId(),
                transferEntity.getId(),
                reasonMessage
        );
    }
    
}
