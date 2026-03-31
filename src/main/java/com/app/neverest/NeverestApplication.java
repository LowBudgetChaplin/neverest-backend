package com.app.neverest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NeverestApplication {

	public static void main(String[] args) {
		SpringApplication.run(NeverestApplication.class, args);
	}

}
