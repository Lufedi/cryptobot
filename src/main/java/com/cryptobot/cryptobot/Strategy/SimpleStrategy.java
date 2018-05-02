package com.cryptobot.cryptobot.Strategy;

import java.util.HashMap;

/**
 * Created by Felipe Díaz on 1/05/2018.
 */
public class SimpleStrategy  implements  Strategy{


    @Override
    public boolean sellSignal(HashMap<String, Integer> indicators) {
        return ((indicators.get("rsi") < 35 &&
            indicators.get("fastd") < 35 &&
            indicators.get("adx") > 30 &&
            indicators.get("plus_di") > 0.5) ||
                (indicators.get("adx") > 65 && indicators.get("plus_di") > 0.5 ));
    }

    @Override
    public boolean buySignal(HashMap<String, Integer> indicators) {
        return false;
    }
}
