package com.cryptobot.cryptobot.repositories;

import com.cryptobot.cryptobot.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Created by Felipe Díaz on 15/04/2018.
 */
public interface TradeRepository  extends JpaRepository<Trade, Long> {
}
