package com.cryptobot.cryptobot.service.exchange;


import com.cryptobot.cryptobot.config.AuthenticationConfig;
import com.cryptobot.cryptobot.dto.BittrexCandle;
import com.cryptobot.cryptobot.dto.BittrexCandleResponse;
import com.cryptobot.cryptobot.dto.Candle;
import com.cryptobot.cryptobot.dto.PoloniexCandle;
import com.cryptobot.cryptobot.exceptions.TradeException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.BaseExchange;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ExchangeServiceImpl implements ExchangeService {


    private final AuthenticationConfig properties;

    @Autowired
    public ExchangeServiceImpl(AuthenticationConfig properties){
        this.properties = properties;
    }

    //Singleton
    private Exchange exchange;
    private BaseExchange baseExchange = new BittrexExchange();

    public Exchange getExchange(){
        if( exchange == null){
            exchange = createExchange();
        }
        return exchange;
    }

    private Exchange createExchange(){
        ExchangeSpecification exchangeSpecification = baseExchange.getDefaultExchangeSpecification();
        exchangeSpecification.setApiKey(properties.getKey());
        exchangeSpecification.setSecretKey(properties.getSecret());
        return ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
    }

    public TimeSeries getTickerHistory(CurrencyPair pair, int interval) throws TradeException{
        String name = this.getExchange().getDefaultExchangeSpecification().getExchangeName();
        List<Candle> candles = null;
        try{
            switch (name){
                case "Poloniex":
                    candles = getPoloniexTickerHistory(pair, interval, exchange);
                    break;
                case "Bittrex":
                    candles = getBittrexTickerHistory(pair, interval, exchange);
                    break;
                default:
            }
        }catch (Exception e){
            throw new TradeException(e.getMessage(), e);
        }


        return parseTickers(candles);

    }

    private List<Candle> getBittrexTickerHistory(CurrencyPair pair, int interval, Exchange exchange) throws  Exception{

        String [] currencies = pair.toString().split("/");
        String market =  currencies[1] +'-'+ currencies[0]; //"BTC-LTC";
        String url = "https://bittrex.com/Api/v2.0/pub/market/GetTicks?marketName=" + market + "&tickInterval=fiveMin&";
        String inputLine;
        StringBuffer response = new StringBuffer();

        log.debug("url  " + url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        in.lines().forEach( line -> response.append(line));
        in.close();

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();

        BittrexCandleResponse bittrexCandleResponse = gson.fromJson(response.toString(), BittrexCandleResponse.class);
        Optional<List<BittrexCandle>> optional = Optional.of( bittrexCandleResponse.getResult() );
        if(optional.isPresent()){
            List<Candle> result = new ArrayList<>();
            optional.get()
                    .stream()
                    .forEach( bittrexCandle -> result.add( bittrexCandle.toCandle()));
            return result;

        }else{
            throw new TradeException("No tickers found");
        }



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


    public TimeSeries parseTickers(List<Candle> candles){
        TimeSeries series = new BaseTimeSeries();
        ZonedDateTime endTime = ZonedDateTime.now();

        candles.stream()
                .forEach( candle -> {
                    long test_timestamp = candle.getDate().longValue();
                    candle.getDate().longValue();
                    ZonedDateTime jpTime =  Instant.ofEpochMilli(test_timestamp).atZone(ZoneId.systemDefault());
                    series.addBar( new BaseBar( jpTime,
                            candle.getOpen().toString(),
                            candle.getHigh().toString(),
                            candle.getLow().toString(),
                            candle.getClose().toString(),
                            candle.getVolume().toString()));
                });
        return  series;
    }
}
