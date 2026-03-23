package com.project.practice.sap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SapApplication {

	public static void main(String[] args) {
		SpringApplication.run(SapApplication.class, args);
	}

}
