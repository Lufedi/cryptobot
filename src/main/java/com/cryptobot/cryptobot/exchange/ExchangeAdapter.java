package com.cryptobot.cryptobot.exchange;

import com.cryptobot.cryptobot.config.AuthenticationConfig;
import com.cryptobot.cryptobot.dto.Candle;
import com.cryptobot.cryptobot.exceptions.TradeException;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
public abstract class ExchangeAdapter {

    private Exchange exchange;



    public Ticker getTicker(CurrencyPair currencyPair) throws TradeException{
        try{
            return this.exchange.getMarketDataService().getTicker(currencyPair);
        }catch (IOException exception){
            log.error("Exception getting ticker {}", exception.getMessage());
            throw new TradeException(exception);
        }
    }

    public String buy(CurrencyPair currencyPair,  BigDecimal quantity,BigDecimal amount) throws TradeException{
        try{

            String orderId = exchange.getTradeService().placeLimitOrder(
                new LimitOrder( Order.OrderType.BID, quantity, currencyPair, null, null, amount));

            log.info("Buying CUR: {} Qty: {} Rate: {}", currencyPair, quantity, amount);
            return orderId;
        }catch (final IOException exception){
            log.error("Error trying to create buy order. Cur: {}, qty: {}, rate: {}", currencyPair, quantity, amount);
            throw new TradeException(exception);
        }
    }

    public String sell(CurrencyPair currencyPair,  BigDecimal quantity,BigDecimal amount) throws TradeException{
        try{
            String orderId = exchange.getTradeService().placeLimitOrder(
                    new LimitOrder( Order.OrderType.ASK, quantity,
                            currencyPair, null, null, amount));
            log.info("Selling CUR: {} Qty: {} Rate: {}", currencyPair, quantity, amount);
            return  orderId;
        }catch (final IOException exception){
            log.error("Error trying to create sell order. Cur: {}, qty: {}, rate: {}", currencyPair, quantity, amount);
            throw new TradeException(exception);
        }
    }



    public BigDecimal getTradingFee() throws  TradeException{
        try{
            return exchange.getAccountService().getAccountInfo().getTradingFee();
        }catch (final IOException exception){
            log.error("Error trying to get trading fee of exchange {}" , this.getName());
            throw new TradeException(exception);
        }
    }

    public String getName() throws  TradeException{
        try{
            return exchange.getDefaultExchangeSpecification().getExchangeName();
        }catch (final Exception exception){
            log.error("Error trying to get exchange name");
            throw new TradeException(exception);
        }
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



    public abstract BigDecimal getBalance(Currency currency) throws TradeException;

    protected abstract List<Candle> getCandles(CurrencyPair currencyPair) throws TradeException;

    public TimeSeries getTimeSeries(CurrencyPair currencyPair) throws  TradeException{
        return  this.parseTickers(this.getCandles(currencyPair));
    }


}
