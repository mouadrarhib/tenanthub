package com.tenanthub.project;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProjectServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(ProjectServiceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ProjectServiceApplication.class, args);
		log.info("Project Service started successfully");
	}

}
