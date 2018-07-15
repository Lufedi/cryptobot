package com.cryptobot.cryptobot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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

    @Column(precision = 10, scale = 10)
    private BigDecimal amount = BigDecimal.ZERO;
    @Column(precision = 10, scale = 10)
    private BigDecimal priceOpen = BigDecimal.ZERO;
    @Column(precision = 10, scale =  10)
    private BigDecimal priceClose = BigDecimal.ZERO;
}
