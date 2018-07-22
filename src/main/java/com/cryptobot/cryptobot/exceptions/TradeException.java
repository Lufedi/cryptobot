package com.cryptobot.cryptobot.exceptions;

public class TradeException extends  Exception {

    public TradeException(String message){
        super(message);
    }

    public TradeException(String message, Throwable e){
        super(message, e);

    }


    @Override
    public String toString(){

        return "Message " + this.getMessage();
    }
}
