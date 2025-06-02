package com.example.ibank.blocker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication( scanBasePackages = "com.example.ibank")
public class BlockerApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlockerApplication.class, args);
	}

}
