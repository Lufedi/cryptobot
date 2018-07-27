package com.cryptobot.cryptobot.service.strategy;

import com.cryptobot.cryptobot.exceptions.TradeException;
import com.cryptobot.cryptobot.service.exchange.ExchangeService;
import com.cryptobot.cryptobot.service.indicators.IndicatorsService;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.ta4j.core.TimeSeries;

import java.util.HashMap;

@Slf4j
@Primary
@Service
public class RsiAndMadcStrategy implements Strategy {


    @Autowired
    ExchangeService exchangeService;

    @Autowired
    private IndicatorsService indicatorsService;

    @Override
    public boolean sellSignal(HashMap<String, Double> indicators) {
        return (indicators.get("rsi") < 50 && indicators.get("madc") > 0);
    }

    @Override
    public boolean buySignal(HashMap<String, Double> indicators) {
        return (indicators.get("rsi") > 50 && indicators.get("madc") > 0);    }

    @Override
    public StrategyResult applyStrategy(CurrencyPair pair, int interval) throws TradeException {
        try{

            TimeSeries timeSeries =  exchangeService.getTickerHistory(pair, interval);
            HashMap<String, Double> indicators =  indicatorsService.calculateIndicators(timeSeries);

            boolean buySignal = this.buySignal(indicators);
            log.debug("buy signal " + buySignal);

            boolean sellSignal  =  this.sellSignal(indicators);
            return new StrategyResult(buySignal, sellSignal);

        }catch (Exception e){
            throw new TradeException(e.toString(), e);
        }
    }
}
