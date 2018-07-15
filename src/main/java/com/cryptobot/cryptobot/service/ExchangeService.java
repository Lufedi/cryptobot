package com.cryptobot.cryptobot.service;


import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.springframework.stereotype.Component;


@Component
public class ExchangeService {


    private String BITTREX_API = "6f422bb6421a43768803bb224f307f98";
    private String BITTREX_API_SECRET = "ac09a22e4c4849f1ad5c0dc9da88fd9d";


    public Exchange getExchange(){

        ExchangeSpecification bittrexSpecification = new BittrexExchange().getDefaultExchangeSpecification();
        bittrexSpecification.setApiKey(BITTREX_API);
        bittrexSpecification.setSecretKey(BITTREX_API_SECRET);
        Exchange bittrexExchange = ExchangeFactory.INSTANCE.createExchange(bittrexSpecification);

        return bittrexExchange;
    }
}
