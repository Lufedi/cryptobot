package com.cryptobot.cryptobot.service;


import com.cryptobot.cryptobot.exceptions.TradeException;
import com.cryptobot.cryptobot.model.Trade;
import com.cryptobot.cryptobot.repositories.TradeRepository;
import com.cryptobot.cryptobot.service.exchange.ExchangeService;
import com.cryptobot.cryptobot.service.strategy.Strategy;
import com.cryptobot.cryptobot.service.strategy.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.bittrex.service.BittrexAccountServiceRaw;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


@Slf4j
@Service
public class CryptobotService {

    private CurrencyPair[] pairs = {
            CurrencyPair.ETH_BTC,
    };

    private int MAXIMUM_TRADES = 1;
    private int TICKER_INTERVAL = 5; //minutes
    private BigDecimal STAKE_AMOUNT = new BigDecimal("0.002");

    private Hashtable<String, BigDecimal> minimunStake= new Hashtable<>();

    @Autowired
    private Strategy strategy;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private ExchangeService exchangeService;


    @Scheduled(fixedRate = 10000)
    public void runBot() throws  Exception{

        try{

            Exchange bittrexExchange = exchangeService.getExchange();
            minimunStake.put(bittrexExchange.toString(), new BigDecimal("0.00001"));

            List<Trade> trades =  tradeRepository.findAll();

            trades.stream().forEach( trade ->{
                try{ tryToSell(trade); }catch(Exception e){ throw  new RuntimeException(e);}
            });

            if( trades.size() < MAXIMUM_TRADES){
                tryToBuy(bittrexExchange);
            }

        }catch (Exception e){
            log.error(e.getMessage());
            throw e;
        }
    }






    private boolean createTrade(Exchange exchange) throws  Exception{

        CurrencyPair buyingPair = null;

         Optional<CurrencyPair> optionalBuyingPair =
                 Arrays.stream(pairs).filter( currencyPair -> {
                                                try{
                                                    return strategy.applyStrategy(currencyPair, TICKER_INTERVAL).buySignal();
                                                }catch (TradeException e){
                                                    throw  new RuntimeException(e.getMessage(), e);
                                                }
                                            })
                                     .findFirst();

        if(!optionalBuyingPair.isPresent()){
            return false;
        }

        buyingPair = optionalBuyingPair.get();

        Ticker exchangeTicker = exchange.getMarketDataService().getTicker(buyingPair);
        BigDecimal buyLimit = getTargetBid(exchangeTicker);
        Optional<BigDecimal> stakeAmount = getStakeAmount(exchange);
        if(!stakeAmount.isPresent()){
            return false;
        }

        BigDecimal quantity = stakeAmount.get().divide(buyLimit, BigDecimal.ROUND_HALF_DOWN);
        
        log.debug("Buying " +  buyingPair + " qty: "+ quantity + " buyLimit: " + buyLimit);

        String orderId = exchange.getTradeService().placeLimitOrder(
                new LimitOrder( Order.OrderType.BID, quantity, buyingPair, null, null, buyLimit)
        );


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

    private  void tryToSell(Trade trade) throws TradeException, IOException{
        if( trade.isOpen() && trade.getOrderId() != null){
            manageTrade(trade);
        }
    }

    private boolean manageTrade(Trade trade) throws  TradeException, IOException{
        if(!trade.isOpen()){
            throw new TradeException("Trying to sell closed trade");
        }
        CurrencyPair tradeCurrency = new CurrencyPair(trade.getPair());
        BigDecimal currentBidRate = exchangeService
                .getExchange()
                .getMarketDataService()
                .getTicker(tradeCurrency)
                .getBid();

        StrategyResult strategyResult = strategy.applyStrategy(tradeCurrency, TICKER_INTERVAL);
        if (shouldSell(trade, currentBidRate) && !strategyResult.buySignal()){
            executeSell(trade, currentBidRate);
            return true;
        }
        return false;
    }


    public void executeSell(Trade trade, BigDecimal limit) throws TradeException{
        try{
            Exchange exchange = this.exchangeService.getExchange();
            CurrencyPair tradeCurrency = new CurrencyPair(trade.getPair());

            String orderId = exchange.getTradeService().placeLimitOrder(
                            new LimitOrder( Order.OrderType.ASK, trade.getQuantity(),
                            tradeCurrency, null, null, limit));

            trade.setCloseOrderId(orderId);
            trade.setPriceClose(limit);
            tradeRepository.save(trade);

            log.debug("Selling CUR: "  + tradeCurrency  + ", QTy: " + trade.getQuantity() + ", rate: " + limit);

        }catch (IOException e){

            throw new TradeException(e.getMessage(), e);
        }


    }

    public boolean shouldSell(Trade trade, BigDecimal bidRate){
        //TODO Add minimun profit validation
        return trade.calculateProfit(bidRate).compareTo(BigDecimal.ZERO) > 0;
    }


    public Optional<BigDecimal> getStakeAmount(Exchange exchange)  throws Exception{
        BittrexAccountServiceRaw bittrexAccountServiceRaw =  (BittrexAccountServiceRaw) exchange.getAccountService();
        BigDecimal balance = bittrexAccountServiceRaw.getBittrexBalance(Currency.BTC).getBalance();

        if(balance.compareTo(STAKE_AMOUNT) >= 1){
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
