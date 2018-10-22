package com.arkaces.bitcoin_ark_lite_channel_service.electrum;

import ark_java_client.lib.ResourceUtils;
import org.bitcoinj.core.NetworkParameters;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Runnable test script for sending a transaction on electrum using the ElectrumService
 */
public class SendTransactionTest {

    public static void main(String[] args) {
        Yaml yaml = new Yaml();
        InputStream fileInputStream = ResourceUtils.getInputStream("electrum_network_config/testnet.yml");
        ElectrumNetworkSettings electrumNetworkSettings = yaml.loadAs(fileInputStream, ElectrumNetworkSettings.class);

        ElectrumRpcClient electrumRpcClient = new ElectrumRpcClient(electrumNetworkSettings.getSeedPeers());
        ElectrumService electrumService = new ElectrumService(electrumRpcClient, NetworkParameters.testNet3());

        String recipientBtcAddress = "change_me";
        String senderPrivateKey = "change_me";

        electrumService.sendTransaction(recipientBtcAddress, new BigDecimal("0.1"), senderPrivateKey);
    }
}
