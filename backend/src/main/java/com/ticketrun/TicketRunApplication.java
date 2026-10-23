package com.ticketrun;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TicketRunApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicketRunApplication.class, args);
	}

}