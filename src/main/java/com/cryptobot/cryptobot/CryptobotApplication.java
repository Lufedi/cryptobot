package com.cryptobot.cryptobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Starting point of the app
 * @author Lufedi
 */
@SpringBootApplication
@EnableScheduling
public class CryptobotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptobotApplication.class, args);
	}
}
