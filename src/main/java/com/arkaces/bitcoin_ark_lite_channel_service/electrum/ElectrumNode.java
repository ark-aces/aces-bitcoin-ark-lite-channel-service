package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import lombok.Data;

@Data
public class ElectrumNode {
    private String host;
    private Integer port;
}
