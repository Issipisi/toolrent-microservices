package com.example.loans_service.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.loans_service.client")
public class FeignConfig {
    // Configuración de Feign
}