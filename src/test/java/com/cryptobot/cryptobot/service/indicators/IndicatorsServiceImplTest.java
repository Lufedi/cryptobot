package com.cryptobot.cryptobot.service.indicators;


import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Hashtable;

import static org.junit.Assert.*;

public class IndicatorsServiceImplTest {


    IndicatorsServiceImpl indicatorsService = new IndicatorsServiceImpl();

    //Local variables
    private BaseTimeSeries baseTimeSeries = new BaseTimeSeries();

    @Before
    public void setUp() throws Exception {

        baseTimeSeries = getCandlesFromFile("testdata/gdax_candles.json");

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



    @Test
    public void calculateIndicators()  throws  Exception{


        HashMap<String, Double> indicators = indicatorsService.calculateIndicators(baseTimeSeries);


        assertEquals(new Double(96.0),  indicators.get("plus_di"));
        assertEquals(new Double(7407.0),  indicators.get("fastd"));
        assertEquals(new Double(0.0),  indicators.get("adx"));
        assertEquals(new Double(45.0),  indicators.get("rsi"));



    }
}