package com.thanh.fastfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FastfoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(FastfoodApplication.class, args);
	}

}
