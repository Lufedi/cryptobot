package com.cryptobot.cryptobot.service.exchange;

import com.cryptobot.cryptobot.config.AuthenticationConfig;
import com.cryptobot.cryptobot.exchange.ExchangeFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExchangeFactoryImplTest {

    @Mock
    AuthenticationConfig authenticationConfig;

    @InjectMocks
    ExchangeFactoryImpl exchangeFactoryImpl;

    @Test
    public void getExchangeTest(){

    }
}
