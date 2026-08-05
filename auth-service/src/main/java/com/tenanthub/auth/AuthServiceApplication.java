package com.tenanthub.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
		log.info("Auth Service started successfully");
	}

}
