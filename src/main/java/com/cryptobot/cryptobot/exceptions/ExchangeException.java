package com.cryptobot.cryptobot.exceptions;

public class ExchangeException extends  Exception {

    /**
     * Builds exception with error message
     * @param message error message
     */
    public ExchangeException(String message) {
        super(message);
    }

    /**
     * Builds exception with error message and throwable exception
     * @param message
     * @param cause throwable exception
     */
    public ExchangeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Build exception with throwable exception
     * @param cause throwable exception
     */
    public ExchangeException(Throwable cause) {
        super(cause);
    }
}
