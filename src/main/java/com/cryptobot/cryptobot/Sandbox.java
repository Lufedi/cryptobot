package com.cryptobot.cryptobot;

import com.cryptobot.cryptobot.DTO.BittrexCandle;
import com.cryptobot.cryptobot.DTO.BittrexCandleResponse;
import com.cryptobot.cryptobot.DTO.PoloniexCandle;
import com.cryptobot.cryptobot.DTO.Candle;
import com.cryptobot.cryptobot.service.strategy.Strategy;
import com.cryptobot.cryptobot.model.Trade;
import com.cryptobot.cryptobot.repositories.TradeRepository;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;

import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.bittrex.service.BittrexAccountServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.adx.PlusDIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.util.*;

/**
 * Created by Felipe Díaz on 15/04/2018.
 */
@Component
public class Sandbox implements CommandLineRunner {


    private TradeRepository tradeRepository;
    private CurrencyPair[] pairs = {
            //CurrencyPair.LTC_BTC,
            CurrencyPair.ETH_BTC,
            //CurrencyPair.DASH_BTC,
            //CurrencyPair.ZEC_BTC,
            };

    /*
      "ETH/BTC",
            "LTC/BTC",
            "ETC/BTC",
            "DASH/BTC",
            "ZEC/BTC",
            "XLM/BTC",
            "NXT/BTC",
            "POWR/BTC",
            "ADA/BTC",
            "XMR/BTC"
     */
    private int MAXIMUM_TRADES = 3;
    private int TICKER_INTERVAL = 5; //minutes
    private BigDecimal STAKE_AMOUNT = new BigDecimal("0.0000001");
    private String BITTREX_API = "6f422bb6421a43768803bb224f307f98";
    private String BITTREX_API_SECRET = "ac09a22e4c4849f1ad5c0dc9da88fd9d";

    private Hashtable<String, BigDecimal>  minimunStake= new Hashtable<>();


    @Autowired
    private Strategy strategy;

    public Sandbox(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public void run(String... strings) throws Exception {


//        internalRun();

    }

    private  void internalRun() throws Exception{
        System.out.println("EXECUTING!!!!!!");
        ExchangeSpecification bittrexSpecification = new BittrexExchange().getDefaultExchangeSpecification();
        bittrexSpecification.setApiKey(BITTREX_API);
        bittrexSpecification.setSecretKey(BITTREX_API_SECRET);
        Exchange bittrexExchange = ExchangeFactory.INSTANCE.createExchange(bittrexSpecification);

        System.out.println("exhange str" + bittrexExchange.toString());
        minimunStake.put(bittrexExchange.toString(), new BigDecimal("0.00001"));


        AccountService accountService = bittrexExchange.getAccountService();
        System.out.println("balance");
        System.out.println(((BittrexAccountServiceRaw) accountService).getBittrexBalance(Currency.BTC));
        System.out.println("balance");

        while(true){

            List<Trade> trades =  tradeRepository.findAll();

            for( Trade trade: trades){
                tryToSell(trade, bittrexExchange);
            }

            if( trades.size() < MAXIMUM_TRADES){
                tryToBuy(bittrexExchange);
            }
            Thread.sleep(300000);
        }

    }

    private boolean[] applyStrategy(CurrencyPair pair, int interval, Exchange exchange)  throws Exception {
        System.out.println("Applying strategy");
        MarketDataService  marketDataService = exchange.getMarketDataService();

        List<Candle> listCandle =    getTickerHistory(pair, interval, exchange);

        TimeSeries timeSeries = parseTickers(listCandle);

        HashMap<String, Integer> indicators = calculateIndicators(timeSeries);

        boolean buySignal = strategy.buySignal(indicators);
        System.out.println("buy signal " + buySignal);

        boolean sellSignal  =  strategy.sellSignal(indicators);

        if(buySignal){
            //exchange.getTradeService().placeMarketOrder( new MarketOrder(Order.OrderType.BID, STAKE_AMOUNT, CurrencyPair.LTC_BTC));
            System.out.println("Buying " + STAKE_AMOUNT.toString() + " LTC");


        }
        boolean a[] = {true, false};
        return  a;
    }


    private HashMap<String , Integer> calculateIndicators(TimeSeries timeSeries){
        HashMap<String, Integer> indicators = new HashMap<>();


        ClosePriceIndicator closePrice = new ClosePriceIndicator(timeSeries);
        int LAST_INDEX = timeSeries.getEndIndex()-1;
        //RSI
        RSIIndicator rsiIndicator  = new RSIIndicator(closePrice, 14);
        int value =  rsiIndicator.getValue(LAST_INDEX).intValue();
        indicators.put( "rsi",value);

        System.out.println("f"+ value + " in " + timeSeries.getEndIndex());

        //ADX
        ADXIndicator adxIndicator = new ADXIndicator(timeSeries, 14);
        value = adxIndicator.getValue( LAST_INDEX).intValue();
        indicators.put( "adx",value);
        System.out.println("adx " + value);

        //PLUS DI
        PlusDIIndicator plusDIIndicator = new PlusDIIndicator(timeSeries, 14);
        value = plusDIIndicator.getValue(LAST_INDEX).intValue();
        indicators.put("plus_di", value);
        System.out.println("plus_di" + value);


        //FASTD
        StochasticOscillatorDIndicator stochasticOscillatorDIndicator = new StochasticOscillatorDIndicator(closePrice);
        value = stochasticOscillatorDIndicator.getValue(LAST_INDEX).intValue();
        indicators.put("fastd", value);
        System.out.println("fast " +value);




        return  indicators;

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

    private boolean tryToBuy(Exchange exchange) throws  Exception, IOException{
        if( createTrade(exchange)){
            return true;
        }else{
            System.out.println("Found no buy signals for whitelisted currencies. Trying again..");
        }
        return false;
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

        BigDecimal amount = stakeAmount.get().divide(buyLimit, BigDecimal.ROUND_HALF_DOWN);
        System.out.println("Buying " +  buyingPair + " Amount: "+ amount);



        Trade trade =  new Trade();
        trade.setExchange(exchange.getDefaultExchangeSpecification().getExchangeName());
        trade.setQuantity(amount);
        trade.isOpen();
        trade.setPair( buyingPair.toString());
        tradeRepository.save(trade);



        return false;
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


        //System.out.println(bittrexCandleResponse.result);
        //System.out.println(bittrexCandleResponse.result.size());

        List<Candle> result = new ArrayList<>();
        for(BittrexCandle bittrexCandle: bittrexCandleResponse.result){
            result.add( bittrexCandle.toCandle());
        }
        System.out.println(result.size());
        System.out.println(result.get(result.size() - 1));
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
        System.out.println("\nSending 'GET' request to URL : " + url);

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


        //System.out.println(tickerList);
        //System.out.println(tickerList.size());

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
}
