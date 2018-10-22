package com.arkaces.bitcoin_ark_lite_channel_service;

import ark_java_client.*;
import ark_java_client.lib.ResourceUtils;
import com.arkaces.aces_server.aces_service.config.AcesServiceConfig;
import com.arkaces.bitcoin_ark_lite_channel_service.electrum.ElectrumNetworkSettings;
import com.arkaces.bitcoin_ark_lite_channel_service.electrum.ElectrumNode;
import org.bitcoinj.core.NetworkParameters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;

@Configuration
@Import({AcesServiceConfig.class})
public class ApplicationConfig {

    @Bean
    public ArkClient arkClient(Environment environment) {
        ArkNetworkFactory arkNetworkFactory = new ArkNetworkFactory();
        String arkNetworkConfigPath = environment.getProperty("arkNetworkConfigPath");
        ArkNetwork arkNetwork = arkNetworkFactory.createFromYml(arkNetworkConfigPath);

        HttpArkClientFactory httpArkClientFactory = new HttpArkClientFactory();
        return httpArkClientFactory.create(arkNetwork);
    }

    @Bean
    public Integer bitcoinMinConfirmations(Environment environment) {
        return environment.getProperty("bitcoinMinConfirmations", Integer.class);
    }

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        
        return eventMulticaster;
    }

    @Bean
    public ElectrumNetworkSettings electrumNetworkSettings(Environment environment) {
        Yaml yaml = new Yaml();
        String configFilename = environment.getProperty("electrumNetworkConfigPath");
        InputStream fileInputStream = ResourceUtils.getInputStream(configFilename);
        return yaml.loadAs(fileInputStream, ElectrumNetworkSettings.class);
    }

    @Bean
    public List<ElectrumNode> electrumSeedPeers(ElectrumNetworkSettings electrumNetworkSettings) {
        return electrumNetworkSettings.getSeedPeers();
    }

    @Bean
    public NetworkParameters bitcoinNetworkParameters(ElectrumNetworkSettings electrumNetworkSettings) {
        switch (electrumNetworkSettings.getNetwork()) {
            case "mainnet":
                return NetworkParameters.prodNet();
            case "testnet":
                return NetworkParameters.testNet3();
            default:
                throw new IllegalArgumentException("Bitcoin network type invalid: " + electrumNetworkSettings.getNetwork());
        }
    }
}
