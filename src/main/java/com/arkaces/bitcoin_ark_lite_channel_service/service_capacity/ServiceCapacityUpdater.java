package com.arkaces.bitcoin_ark_lite_channel_service.service_capacity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ServiceCapacityUpdater {

    private final ServiceCapacityService serviceCapacityService;

    @Scheduled(fixedDelayString = "${capacityUpdateIntervalSec}000")
    public void sweep() {
        try {
            log.info("Updating capacity");
            serviceCapacityService.updateCapacities();
        } catch (Exception e) {
            log.error("Failed to update capacity", e);
        }
    }

}