package com.cryptobot.cryptobot.service.strategy;

import org.springframework.stereotype.Component;


public class StrategyResult{

    private boolean sell;
    private boolean buy;

    public StrategyResult(boolean buy, boolean sell){
        this.buy = buy;
        this.sell = sell;
    }
    public boolean buySignal(){
        return this.buy;
    }
    public boolean sellSignal(){
        return  this.sell;
    }
}