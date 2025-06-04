package com.example.ibank.exrate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackages = "com.example.ibank")
public class ExrateApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExrateApplication.class, args);
	}

}
