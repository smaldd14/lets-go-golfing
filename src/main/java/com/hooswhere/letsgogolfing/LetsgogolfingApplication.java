package com.hooswhere.letsgogolfing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.hooswhere.letsgogolfing")
public class LetsgogolfingApplication {

	public static void main(String[] args) {
		SpringApplication.run(LetsgogolfingApplication.class, args);
	}

}
