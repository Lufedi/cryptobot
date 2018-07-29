package com.cryptobot.cryptobot.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by Felipe Díaz on 16/04/2018.
 */
@Data
public class Candle {

    BigDecimal date;
    BigDecimal high;
    BigDecimal low;
    BigDecimal open;
    BigDecimal close;
    BigDecimal volume;

    public String toString() {
        return "Ticker: [date=" + date + ", high=" + high +", low:"+ low +", open:"+ open + ", close:"+ close + ", volume:" + volume;
    }
}
