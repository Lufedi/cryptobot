package com.cryptobot.cryptobot.service.exchange;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.Exchange;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeServiceImplTest {

    @InjectMocks
    ExchangeServiceImpl exchangeService;

    @Test
    public void getExchangeTest() {
        Exchange exchange = exchangeService.getExchange();
        Exchange exchange2 = exchangeService.getExchange();
        assertEquals(exchange, exchange2);
    }

    @Test
    public void createExchangeTest(){
        Exchange exchange = exchangeService.createExchange();
        assertNotNull(exchange);
    }



}