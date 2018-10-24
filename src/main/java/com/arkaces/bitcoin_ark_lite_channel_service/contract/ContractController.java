package com.arkaces.bitcoin_ark_lite_channel_service.contract;

import com.arkaces.aces_server.aces_service.contract.Contract;
import com.arkaces.aces_server.aces_service.contract.ContractStatus;
import com.arkaces.aces_server.aces_service.contract.CreateContractRequest;
import com.arkaces.aces_server.aces_service.error.ServiceErrorCodes;
import com.arkaces.aces_server.common.error.NotFoundException;
import com.arkaces.aces_server.common.identifer.IdentifierGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ContractController {
    
    private final IdentifierGenerator identifierGenerator;
    private final ContractRepository contractRepository;
    private final ContractMapper contractMapper;
    private final CreateContractRequestValidator contractRequestValidator;
    private final NetworkParameters bitcoinNetworkParameters;

    @PostMapping("/contracts")
    public Contract<Results> postContract(@RequestBody CreateContractRequest<Arguments> createContractRequest) {
        contractRequestValidator.validate(createContractRequest);

        ECKey ecKey = new ECKey();
        Address address = ecKey.toAddress(bitcoinNetworkParameters);
        String depositBtcAddress = address.toBase58();
        String depositBtcAddressPrivateKey = ecKey.getPrivateKeyEncoded(bitcoinNetworkParameters).toBase58();

        ContractEntity contractEntity = new ContractEntity();
        contractEntity.setId(identifierGenerator.generate());
        contractEntity.setCorrelationId(createContractRequest.getCorrelationId());
        contractEntity.setReturnBtcAddress(createContractRequest.getArguments().getReturnBtcAddress());
        contractEntity.setRecipientArkAddress(createContractRequest.getArguments().getRecipientArkAddress());
        contractEntity.setCreatedAt(LocalDateTime.now());
        contractEntity.setStatus(ContractStatus.EXECUTED);
        contractEntity.setDepositBtcAddress(depositBtcAddress);
        contractEntity.setDepositBtcAddressPrivateKey(depositBtcAddressPrivateKey);
        contractRepository.save(contractEntity);

        return contractMapper.map(contractEntity);
    }
    
    @GetMapping("/contracts/{contractId}")
    public Contract<Results> getContract(@PathVariable String contractId) {
        ContractEntity contractEntity = contractRepository.findOneById(contractId);
        if (contractEntity == null) {
            throw new NotFoundException(ServiceErrorCodes.CONTRACT_NOT_FOUND, "Contract not found with id = " + contractId);
        }
        
        return contractMapper.map(contractEntity);
    }

}
