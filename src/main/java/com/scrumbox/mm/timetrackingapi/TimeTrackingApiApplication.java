package com.scrumbox.mm.timetrackingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

// @EnableCaching
@SpringBootApplication
@EnableDiscoveryClient
public class TimeTrackingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TimeTrackingApiApplication.class, args);
	}

}
