package com.example.tools_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.example.tools_service.client")
public class ToolsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToolsServiceApplication.class, args);
	}

}
