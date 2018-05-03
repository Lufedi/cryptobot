package com.cryptobot.cryptobot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

/**
 * Created by Felipe Díaz on 15/04/2018.
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
    private BigDecimal amount;
    private BigDecimal priceOpen;
    private BigDecimal priceClose;
}
