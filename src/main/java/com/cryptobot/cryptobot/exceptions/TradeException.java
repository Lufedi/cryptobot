package com.cryptobot.cryptobot.exceptions;

/**
 * Shutdown the bot
 * @author Felipe
 */
public class TradeException extends  Exception {

    /**
     * Builds exception with error message
     * @param message error message
     */
    public TradeException(String message){
        super(message);
    }

    /**
     * Builds exception with error message and throwable exception
     * @param message
     * @param e throwable exception
     */
    public TradeException(String message, Throwable e){
        super(message, e);

    }

    /**
     * Build exception with throwable exception
     * @param e throwable exception
     */
    public TradeException(Throwable e){
        super(e);
    }


    @Override
    public String toString(){

        return "Message " + this.getMessage();
    }
}
