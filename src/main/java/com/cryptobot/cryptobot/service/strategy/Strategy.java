package com.cryptobot.cryptobot.service.strategy;

import com.cryptobot.cryptobot.exceptions.TradeException;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.stereotype.Component;
import org.ta4j.core.TimeSeries;

import java.util.HashMap;

/**
 * Created by Felipe Díaz on 1/05/2018.
 */
public interface Strategy {

    public boolean sellSignal(HashMap<String , Double> indicators );
    public boolean buySignal(HashMap<String , Double> indicators );
    public StrategyResult applyStrategy(CurrencyPair pair, TimeSeries timeSeries) throws TradeException;
}
