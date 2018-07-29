package com.cryptobot.cryptobot.service.indicators;

import org.springframework.stereotype.Service;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.util.HashMap;

@Service
public class IndicatorsServiceImpl  implements  IndicatorsService{
    @Override
    public HashMap<String, Double> calculateIndicators(TimeSeries timeSeries) {
        HashMap<String, Double> indicators = new HashMap<>();


        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        int LAST_INDEX = timeSeries.getEndIndex()-1;
        //RSI
        RSIIndicator rsiIndicator  = new RSIIndicator(closePrice, 14);
        double value =  rsiIndicator.getValue(LAST_INDEX).doubleValue();
        indicators.put( "rsi",value);

        //ADX
        ADXIndicator adxIndicator = new ADXIndicator(timeSeries, 14);
        value = adxIndicator.getValue( LAST_INDEX).intValue();
        indicators.put( "adx",value);

        //PLUS DI
        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(timeSeries, 14);
        value = plusDIIndicator.getValue(LAST_INDEX).doubleValue();
        indicators.put("plus_di", value);

        //FASTD
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator = new StochasticOscillatorDIndicator(closePrice);
        value = stochasticOscillatorDIndicator.getValue(LAST_INDEX).doubleValue();
        indicators.put("fastd", value);

        MACDIndicator macdIndicator = new MACDIndicator(closePrice);
        value = macdIndicator.getValue(LAST_INDEX).doubleValue();
        indicators.put("madc", value);



        return  indicators;

    }
}
