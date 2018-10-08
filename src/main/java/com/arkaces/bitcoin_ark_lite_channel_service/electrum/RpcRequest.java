package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RpcRequest {
    private String jsonrpc = "2.0";
    private String id = UUID.randomUUID().toString();
    private String method;
    private List<Object> params;
}
