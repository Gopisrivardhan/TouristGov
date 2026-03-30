package com.tourismgov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TourismgovApp {

	public static void main(String[] args) {
		SpringApplication.run(TourismgovApp.class, args);
	}

}
