package com.arkaces.bitcoin_ark_lite_channel_service.contract;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<ContractEntity, Long> {
    ContractEntity findOneById(String id);

    ContractEntity findOneBySubscriptionId(String subscriptionId);
    
    ContractEntity findOneByCorrelationId(String correlationId);
}
