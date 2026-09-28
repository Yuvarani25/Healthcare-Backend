package com.healthcare.analyticsgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AnalyticsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsGatewayApplication.class, args);
    }
}
