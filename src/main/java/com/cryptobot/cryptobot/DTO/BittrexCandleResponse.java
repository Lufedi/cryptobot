package com.cryptobot.cryptobot.DTO;

import lombok.Data;

import java.util.List;

/**
 * Created by Felipe Díaz on 1/05/2018.
 */
@Data
public class BittrexCandleResponse {

/*
"success" : true,
	"message" : "",
	"result" : [{
			"MarketName" : "BTC-LTC",
			"High" : 0.01350000,
			"Low" : 0.01200000,
			"Volume" : 3833.97619253,
			"Last" : 0.01349998,
			"BaseVolume" : 47.03987026,
			"TimeStamp" : "2014-07-09T07:22:16.72",
			"Bid" : 0.01271001,
			"Ask" : 0.01291100,
			"OpenBuyOrders" : 45,
			"OpenSellOrders" : 45,
			"PrevDay" : 0.01229501,
			"Created" : "2014-02-13T00:00:00",
			"DisplayMarketName" : null
		}
    ]
 */

    public boolean success;
    public String message;
    public List<BittrexCandle> result;


}
