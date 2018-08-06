package com.cryptobot.cryptobot.exchange;

import com.cryptobot.cryptobot.config.AuthenticationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public  class ExchangeFactoryImpl implements  ExchangeFactory{

    private final AuthenticationConfig config;

    public ExchangeFactoryImpl(AuthenticationConfig config){
        this.config = config;
    }

    @Override
    public  ExchangeAdapter getExchange() {

        ExchangeAdapter exchangeAdapter = null;
        switch (config.getExchangeName()){
            case "bittrex":
                exchangeAdapter = new BittrexExchangeAdapter(config);
            default:
        }

        return exchangeAdapter;
    }
}
