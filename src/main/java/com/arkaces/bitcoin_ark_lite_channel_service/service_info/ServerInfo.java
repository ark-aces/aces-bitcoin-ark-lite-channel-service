package com.arkaces.bitcoin_ark_lite_channel_service.service_info;

import com.arkaces.aces_server.aces_service.server_info.Capacity;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ServerInfo {
    private String name;
    private String description;
    private String version;
    private String websiteUrl;
    private String instructions;
    private List<Capacity> capacities;
    private BigDecimal flatFee;
    private String flatFeeUnit;
    private BigDecimal percentFee;
    private JsonNode inputSchema;
    private JsonNode outputSchema;
    private List<String> interfaces;
}
