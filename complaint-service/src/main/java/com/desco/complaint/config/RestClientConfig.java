package com.desco.complaint.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${notification.service.url:http://notification-service:8084}")
    private String notificationServiceUrl;

    @Bean
    public RestClient notificationRestClient() {
        return RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}
