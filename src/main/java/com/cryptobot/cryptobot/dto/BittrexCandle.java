package com.cryptobot.cryptobot.dto;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Lufedi
 */
public class BittrexCandle  extends Candle{
    /*
  {"O":0.00003030,"H":0.00003035,"L":0.00002972,"C":0.00002972,"V":30086.83868808,"T":"2018-03-22T06:30:00","BV":0.90428201}
    BigDecimal date;
    BigDecimal high;
    BigDecimal low;
    BigDecimal open;
    BigDecimal close;
    BigDecimal volume;

     */

    public BigDecimal last;
    public Date T;
    BigDecimal O, H, L, C, V;


    public Candle toCandle(){
        this.date = new BigDecimal(this.T.getTime());
        this.open = this.O;
        this.close = this.C;
        this.high = this.H;
        this.low = this.L;
        this.volume = this.V;
        return (Candle) this;
    }

    @Override
    public String toString() {
        return "BittrexCandle{" +
                "last=" + last +
                ", T=" + T +
                ", O=" + O +
                ", H=" + H +
                ", L=" + L +
                ", C=" + C +
                ", V=" + V +
                '}';
    }
}
