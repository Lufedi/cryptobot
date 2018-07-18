package com.cryptobot.cryptobot.service.indicators;


import org.ta4j.core.TimeSeries;

import java.util.HashMap;

public interface IndicatorsService {

    public HashMap<String , Integer> calculateIndicators(TimeSeries timeSeries);
}
