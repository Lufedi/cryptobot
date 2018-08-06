package com.cryptobot.cryptobot.exchange;

import com.cryptobot.cryptobot.config.AuthenticationConfig;
import com.cryptobot.cryptobot.dto.Candle;
import com.cryptobot.cryptobot.exceptions.TradeException;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.BittrexExchange;
import org.knowm.xchange.bittrex.dto.marketdata.BittrexChartData;
import org.knowm.xchange.bittrex.service.BittrexAccountServiceRaw;
import org.knowm.xchange.bittrex.service.BittrexChartDataPeriodType;
import org.knowm.xchange.bittrex.service.BittrexMarketDataServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BittrexExchangeAdapter  extends ExchangeAdapter {

    Exchange exchange;

    public BittrexExchangeAdapter(AuthenticationConfig config){
        Exchange baseExchange = new BittrexExchange();
        ExchangeSpecification exchangeSpecification = baseExchange.getDefaultExchangeSpecification();
        exchangeSpecification.setApiKey(config.getKey());
        exchangeSpecification.setSecretKey(config.getSecret());
        this.exchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification);

    }

    @Override
    public BigDecimal getBalance(Currency currency)  throws TradeException{
        try{
            BittrexAccountServiceRaw bittrexAccountServiceRaw =  (BittrexAccountServiceRaw) exchange.getAccountService();
            BigDecimal balance = bittrexAccountServiceRaw.getBittrexBalance(Currency.BTC).getBalance();
        }catch (IOException exception){
            log.error("Error trying to get balance of {}", currency);
            throw new TradeException(exception);
        }
        return null;
    }

    @Override
    public List<Candle> getCandles( CurrencyPair currencyPair ) throws TradeException{
        try{
            BittrexMarketDataServiceRaw bittrexMarketDataServiceRaw =
                    (BittrexMarketDataServiceRaw) exchange.getMarketDataService();

            ArrayList<BittrexChartData> bittrexCandles = bittrexMarketDataServiceRaw.getBittrexChartData(
                   currencyPair, BittrexChartDataPeriodType.FIVE_MIN);


            return bittrexCandles
                    .stream()
                    .map( btxCandle -> new Candle( new BigDecimal(btxCandle.getTimeStamp().getTime()),
                                                                    btxCandle.getHigh(),
                                                                    btxCandle.getLow(),
                                                                    btxCandle.getOpen(),
                                                                    btxCandle.getClose(),
                                                                    btxCandle.getVolume()))
                    .collect(Collectors.toList());


        }catch (final IOException exception){
            log.error("Error trying to get tickers of exchange {}", this.getName());
            throw new TradeException(exception);
        }

    }



}
