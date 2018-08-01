package com.cryptobot.cryptobot.service.exchange;

import com.cryptobot.cryptobot.config.AuthenticationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.Exchange;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeServiceImplTest {

    @Mock
    AuthenticationConfig authenticationConfig;


    @InjectMocks
    ExchangeServiceImpl exchangeService;

    @Before
    public void setUp() throws Exception {
        when(authenticationConfig.getSecret()).thenReturn("mysecret");
        when(authenticationConfig.getKey()).thenReturn("mykey");
    }

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