package com.cryptobot.cryptobot.service.strategy;

import com.cryptobot.cryptobot.exceptions.TradeException;
import com.cryptobot.cryptobot.service.exchange.ExchangeService;
import com.cryptobot.cryptobot.service.indicators.IndicatorsService;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SimpleStrategyImplTest {




    @Mock
    ExchangeService exchangeService;

    @Mock
    IndicatorsService indicatorsService;

    @InjectMocks
    SimpleStrategyImpl simpleStrategy;


    //Local variables
    private BaseTimeSeries baseTimeSeries = new BaseTimeSeries();

    HashMap<String, Double> indicators;

    @Before
    public void setUp() throws Exception {

        indicators = new HashMap<>();
        indicators.put("rsi", 30.0);
        indicators.put("fastd", 25.0);
        indicators.put("adx", 40.0);
        indicators.put("plus_di", 0.7);
        indicators.put("minus_di", 0.7);
    }

    @Test
    public void buySignal() {

        indicators = new HashMap<>();
        indicators.put("rsi", 30.0);
        indicators.put("fastd", 25.0);
        indicators.put("adx", 40.0);
        indicators.put("plus_di", 0.7);


        assertTrue(simpleStrategy.buySignal(indicators));

        indicators = new HashMap<>();
        indicators.put("rsi", 40.0);
        indicators.put("fastd", 25.0);
        indicators.put("adx", 70.0);
        indicators.put("plus_di", 0.7);

        assertTrue(simpleStrategy.buySignal(indicators));


        indicators = new HashMap<>();
        indicators.put("rsi", 45.0);
        indicators.put("fastd", 25.0);
        indicators.put("adx", 40.0);
        indicators.put("plus_di", 0.7);
        assertFalse(simpleStrategy.buySignal(indicators));


    }

    @Test
    public void sellSignal() {



        indicators = new HashMap<>();
        indicators.put("adx", 40.0);
        indicators.put("minus_di", 0.7);


        assertTrue(simpleStrategy.sellSignal(indicators));

        indicators = new HashMap<>();
        indicators.put("adx", 88.0);
        indicators.put("minus_di", 0.7);

        assertTrue(simpleStrategy.sellSignal(indicators));

        indicators = new HashMap<>();
        indicators.put("adx",60.0);
        indicators.put("minus_di", -0.5);

        assertFalse(simpleStrategy.sellSignal(indicators));


    }

    @Test
    public void applyStrategy() throws Exception {


        BaseTimeSeries baseTimeSeries = getCandlesFromFile("testdata/gdax_candles.json");
        when(indicatorsService.calculateIndicators( any(TimeSeries.class) )).thenReturn( indicators);

        //True, True
        when(exchangeService.getTickerHistory(any(CurrencyPair.class), anyInt())).thenReturn(baseTimeSeries);
        StrategyResult signals =  simpleStrategy.applyStrategy(CurrencyPair.ETH_BTC, 5);
        assertTrue(signals.buySignal());
        assertTrue(signals.sellSignal());


        //False , False
        indicators = new HashMap<>();
        indicators.put("rsi", 40.0);
        indicators.put("fastd", 25.0);
        indicators.put("plus_di", 0.7);
        indicators.put("adx",60.0);
        indicators.put("minus_di", -0.5);

        when(indicatorsService.calculateIndicators( any(TimeSeries.class) )).thenReturn( indicators);
        signals =  simpleStrategy.applyStrategy(CurrencyPair.ETH_BTC, 5);
        assertFalse(signals.buySignal());
        assertFalse(signals.sellSignal());

    }



    private BaseTimeSeries getCandlesFromFile(String fileRelativePath) throws  Exception{

        //Using https://api.gdax.com/products/BTC-USD/candles?granularity=60 API
        BaseTimeSeries baseTimeSeries = new BaseTimeSeries();
        String jsonAsString = StreamUtils.copyToString(
                new ClassPathResource("testdata/gdax_candles.json").getInputStream(),
                Charset.defaultCharset()  );
        JSONArray jsonArray =  new JSONArray( jsonAsString);

        for(int i = jsonArray.length() -1 ; i>= 0; i--){
            JSONArray candle = jsonArray.getJSONArray(i);
            long test_timestamp = new Long(candle.getLong(0));
            ZonedDateTime jpTime =  Instant.ofEpochMilli(test_timestamp).atZone(ZoneId.systemDefault());
            baseTimeSeries.addBar(new BaseBar(
                    jpTime,
                    candle.getDouble(1),
                    candle.getDouble(2),
                    candle.getDouble(3),
                    candle.getDouble(4),
                    candle.getDouble(5)
            ));
        }
        return baseTimeSeries;
    }

}