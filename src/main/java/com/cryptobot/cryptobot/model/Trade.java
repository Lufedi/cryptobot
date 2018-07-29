package com.cryptobot.cryptobot.model;

import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Domain object representing the trades executed by the Bot
 *
 * @author Lufedi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String exchange;
    private String pair;
    private boolean open;
    private String orderId;
    private String closeOrderId;


    @Column(precision = 10, scale = 10)
    private BigDecimal fee = BigDecimal.ZERO;
    @Column(precision = 10, scale = 10)
    private BigDecimal quantity = BigDecimal.ZERO;
    @Column(precision = 10, scale = 10)
    private BigDecimal priceOpen = BigDecimal.ZERO;
    @Column(precision = 10, scale =  10)
    private BigDecimal priceClose = BigDecimal.ZERO;

    public BigDecimal calculateProfit(BigDecimal closingPrice){
        return  closingPrice.subtract(priceOpen.subtract(fee));
    }



}
