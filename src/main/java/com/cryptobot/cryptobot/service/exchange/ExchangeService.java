package com.cryptobot.cryptobot.service.exchange;


import com.cryptobot.cryptobot.exceptions.TradeException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.ta4j.core.TimeSeries;

public interface ExchangeService {

    public Exchange getExchange();

    public TimeSeries getTickerHistory(CurrencyPair pair, int interval) throws TradeException;



}
