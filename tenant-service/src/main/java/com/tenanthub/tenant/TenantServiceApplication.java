package com.tenanthub.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TenantServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(TenantServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(TenantServiceApplication.class, args);
		log.info("Tenant Service started successfully");
	}

}
