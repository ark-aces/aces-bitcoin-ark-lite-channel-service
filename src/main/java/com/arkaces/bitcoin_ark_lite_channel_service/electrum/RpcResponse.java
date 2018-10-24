package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import lombok.Data;

@Data
public class RpcResponse<T> {
    private String jsonrpc;
    private T result;
    private Object error;
    private String id;
}
