package com.cryptobot.cryptobot.service.strategy;

import com.cryptobot.cryptobot.DTO.BittrexCandle;
import com.cryptobot.cryptobot.DTO.BittrexCandleResponse;
import com.cryptobot.cryptobot.DTO.Candle;
import com.cryptobot.cryptobot.DTO.PoloniexCandle;
import com.cryptobot.cryptobot.exceptions.TradeException;
import com.cryptobot.cryptobot.service.indicators.IndicatorsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Felipe Díaz on 1/05/2018.
 */

@Slf4j
@Component
public class SimpleStrategyImpl implements  Strategy{


    @Autowired
    private IndicatorsService indicatorsService;

    @Override
    public boolean buySignal(HashMap<String, Integer> indicators) {
        return ((indicators.get("rsi") < 35 &&
            indicators.get("fastd") < 35 &&
            indicators.get("adx") > 30 &&
            indicators.get("plus_di") > 0.5) ||
                (indicators.get("adx") > 65 && indicators.get("plus_di") > 0.5 ));
    }

    @Override
    public boolean sellSignal(HashMap<String, Integer> indicators) {
        return ((indicators.get("adx") > 10 && indicators.get("minus_di") >0) ||
                (indicators.get("adx") > 70 && indicators.get("minus_di") > 0.5));
    }




    public boolean[] applyStrategy(CurrencyPair pair, int interval, Exchange exchange)  throws TradeException {
        try{
            MarketDataService marketDataService = exchange.getMarketDataService();

            List<Candle> listCandle =    getTickerHistory(pair, interval, exchange);

            TimeSeries timeSeries = parseTickers(listCandle);

            HashMap<String, Integer> indicators =  indicatorsService.calculateIndicators(timeSeries);

            boolean buySignal = this.buySignal(indicators);
            log.debug("buy signal " + buySignal);

            boolean sellSignal  =  this.sellSignal(indicators);


            boolean a[] = {buySignal, sellSignal};
            return  a;
        }catch (Exception e){
            throw new TradeException(e.getMessage());
        }

    }




    private List<Candle> getTickerHistory(CurrencyPair pair, int interval, Exchange exchange) throws Exception{
        String name = exchange.getDefaultExchangeSpecification().getExchangeName();
        List<Candle> candles = null;

        switch (name){
            case "Poloniex":
                candles = getPoloniexTickerHistory(pair, interval, exchange);
                break;
            case "Bittrex":
                candles = getBittrexTickerHistory(pair, interval, exchange);
                break;
            default:
        }

        return candles;
    }

    private TimeSeries parseTickers(List<Candle> candles){
        TimeSeries series = new BaseTimeSeries();
        ZonedDateTime endTime = ZonedDateTime.now();
        //BaseBar(java.time.ZonedDateTime endTime, double openPrice, double highPrice, double lowPrice, double closePrice, double volume)
        for(Candle candle: candles){
            long test_timestamp = candle.getDate().longValue();
            ZonedDateTime jpTime =  Instant.ofEpochMilli(test_timestamp).atZone(ZoneId.systemDefault());

            series.addBar( new BaseBar( jpTime,
                    candle.getOpen().toString(),
                    candle.getHigh().toString(),
                    candle.getLow().toString(),
                    candle.getClose().toString(),
                    candle.getVolume().toString()));
        }

        return  series;
    }




    private List<Candle> getBittrexTickerHistory(CurrencyPair pair, int interval, Exchange exchange) throws  Exception{
        String market = "BTC-LTC";
        String url = "https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=" + market + "&tickInterval=fiveMin&";


        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
        //Type collectionType = new TypeToken<List<PoloniexCandle>>() {}.getType();
        BittrexCandleResponse bittrexCandleResponse = gson.fromJson(response.toString(), BittrexCandleResponse.class);

        List<Candle> result = new ArrayList<>();
        for(BittrexCandle bittrexCandle: bittrexCandleResponse.result){
            result.add( bittrexCandle.toCandle());
        }

        return result;

    }
    // HTTP GET request
    private List<Candle> getPoloniexTickerHistory(CurrencyPair pair, int interval, Exchange exchange) throws Exception {
        String currencyPairSymbol = "BTC_NXT";
        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.now());
        LocalDateTime startTime = endTime.minusDays(10);
        ZoneId zoneId = ZoneId.systemDefault();

        long startTimeStamp = startTime.atZone(zoneId).toEpochSecond();
        long endTimeStamp = endTime.atZone(zoneId).toEpochSecond();

        interval *= 60;

        String url = "https://poloniex.com/public?command=returnChartData&currencyPair="+ currencyPairSymbol+"&start="+startTimeStamp+"&end="+endTimeStamp +"&period="+ interval;

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        log.debug("\nSending 'GET' request to URL : " + url);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Gson gson = new GsonBuilder().create();
        Type collectionType = new TypeToken<List<PoloniexCandle>>() {}.getType();
        List<PoloniexCandle> tickerList = gson.fromJson(response.toString(), collectionType );


        List<Candle> result = new ArrayList<>();
        for(PoloniexCandle poloniexTickerDao: tickerList){
            result.add( (Candle) poloniexTickerDao);
        }



        return result;
    }


}
