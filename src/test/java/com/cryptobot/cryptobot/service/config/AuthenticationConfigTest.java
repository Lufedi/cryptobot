package com.cryptobot.cryptobot.service.config;


import com.cryptobot.cryptobot.config.AuthenticationConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

public class AuthenticationConfigTest {

    private AuthenticationConfig authenticationConfig;

    @Before
    public void  setUp(){
        authenticationConfig = new AuthenticationConfig();
        authenticationConfig.setExchangeName("bittrex");
        authenticationConfig.setKey("818181j2j2u12u12uh1u2hu1h21u2");
        authenticationConfig.setSecret("p1op2o12p1op29399399iooiioioo");
    }

    @Test
    public void authenticationConfigValuesTest(){

        assertEquals(authenticationConfig.getExchangeName(), "bittrex");
        assertEquals(authenticationConfig.getKey(),"818181j2j2u12u12uh1u2hu1h21u2");
        assertEquals(authenticationConfig.getSecret(),"p1op2o12p1op29399399iooiioioo");
    }

}
