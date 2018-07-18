package com.cryptobot.cryptobot.service;


import com.cryptobot.cryptobot.DTO.BittrexCandle;
import com.cryptobot.cryptobot.DTO.BittrexCandleResponse;
import com.cryptobot.cryptobot.DTO.Candle;
import com.cryptobot.cryptobot.DTO.PoloniexCandle;
import com.cryptobot.cryptobot.model.Trade;
import com.cryptobot.cryptobot.repositories.TradeRepository;
import com.cryptobot.cryptobot.service.exchange.ExchangeService;
import com.cryptobot.cryptobot.service.indicators.IndicatorsService;
import com.cryptobot.cryptobot.service.strategy.Strategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.bittrex.service.BittrexAccountServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.util.*;


@Slf4j
@Service
public class CryptobotService {

    private CurrencyPair[] pairs = {
            //CurrencyPair.LTC_BTC,
            CurrencyPair.ETH_BTC,
            //CurrencyPair.DASH_BTC,
            //CurrencyPair.ZEC_BTC,
    };
    private int MAXIMUM_TRADES = 1;
    private int TICKER_INTERVAL = 5; //minutes
    private BigDecimal STAKE_AMOUNT = new BigDecimal("0.002");

    private Hashtable<String, BigDecimal> minimunStake= new Hashtable<>();



    @Autowired
    private Strategy strategy;

    private TradeRepository tradeRepository;

    private ExchangeService exchangeService;

    private IndicatorsService indicatorsService;

    public CryptobotService(TradeRepository tradeRepository, ExchangeService exchangeService, IndicatorsService indicatorsService) {
        this.tradeRepository = tradeRepository;
        this.exchangeService = exchangeService;
        this.indicatorsService = indicatorsService;
    }

    @Scheduled(fixedRate = 10000)
    public void runBot() throws  Exception{

        try{

            Exchange bittrexExchange = exchangeService.getExchange();
            minimunStake.put(bittrexExchange.toString(), new BigDecimal("0.00001"));

            List<Trade> trades =  tradeRepository.findAll();

            for( Trade trade: trades){
                tryToSell(trade, bittrexExchange);
            }

            if( trades.size() < MAXIMUM_TRADES){
                tryToBuy(bittrexExchange);
            }



        }catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }




    private boolean[] applyStrategy(CurrencyPair pair, int interval, Exchange exchange)  throws Exception {

        MarketDataService marketDataService = exchange.getMarketDataService();

        List<Candle> listCandle =    getTickerHistory(pair, interval, exchange);

        TimeSeries timeSeries = parseTickers(listCandle);

        HashMap<String, Integer> indicators =  indicatorsService.calculateIndicators(timeSeries);

        boolean buySignal = strategy.buySignal(indicators);
        log.debug("buy signal " + buySignal);

        boolean sellSignal  =  strategy.sellSignal(indicators);

        if(buySignal){
            //
            log.debug("Buying " + STAKE_AMOUNT.toString() + " LTC");


        }
        boolean a[] = {true, false};
        return  a;
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


    private boolean createTrade(Exchange exchange) throws  Exception{

        CurrencyPair buyingPair = null;

        for(CurrencyPair pair: pairs){
            boolean buySell[] = applyStrategy(pair, TICKER_INTERVAL, exchange);
            if (buySell[0]){
                buyingPair = pair;
                break;
            }
        }


        if( buyingPair == null){
            return false;
        }

        Ticker exchangeTicker = exchange.getMarketDataService().getTicker(buyingPair);
        BigDecimal buyLimit = getTargetBid(exchangeTicker);
        Optional<BigDecimal> stakeAmount = getStakeAmount(exchange);
        if(!stakeAmount.isPresent()){
            return false;
        }

        BigDecimal quantity = stakeAmount.get().divide(buyLimit, BigDecimal.ROUND_HALF_DOWN);
        
        log.debug("Buying " +  buyingPair + " qty: "+ quantity + " buyLimit: " + buyLimit);
        String orderId = "pp";
        /*String orderId = exchange.getTradeService().placeLimitOrder(
                new LimitOrder( Order.OrderType.BID, quantity, buyingPair, null, null, buyLimit)
        );*/




        BigDecimal fee = exchange.getAccountService().getAccountInfo().getTradingFee();
        //Create trade
        Trade trade =  new Trade();
        trade.setExchange(exchange.getDefaultExchangeSpecification().getExchangeName());
        trade.setQuantity(quantity);
        trade.isOpen();
        trade.setPair( buyingPair.toString());
        trade.setOrderId(orderId);
        trade.setFee(fee);
        trade.setPriceOpen(buyLimit);

        tradeRepository.save(trade);


        return true;
    }


    private BigDecimal getTargetBid(Ticker ticker){
        if( ticker.getAsk().compareTo(ticker.getLast()) < 0 ){
            return  ticker.getAsk();
        }else{
            return ticker.getAsk().add(ticker.getLast().subtract(ticker.getAsk()));
        }
    }

    private  void tryToSell(Trade trade, Exchange exchange){
        if( trade.isOpen()){

        }
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


    public Optional<BigDecimal> getStakeAmount(Exchange exchange)  throws Exception{
        BittrexAccountServiceRaw bittrexAccountServiceRaw =  (BittrexAccountServiceRaw) exchange.getAccountService();
        BigDecimal balance = bittrexAccountServiceRaw.getBittrexBalance(Currency.BTC).getBalance();

        if( balance.compareTo(STAKE_AMOUNT) >= 1){
            return Optional.of(STAKE_AMOUNT);
        }
        return Optional.empty();
    }

    private boolean tryToBuy(Exchange exchange) throws  Exception, IOException {
        if( createTrade(exchange)){
            return true;
        }else{
            log.debug("Found no buy signals for whitelisted currencies. Trying again..");
        }
        return false;
    }

}
