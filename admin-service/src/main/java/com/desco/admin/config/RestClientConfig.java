package com.desco.admin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${desco.health-timeout-ms:1500}")
    private int healthTimeoutMs;

    @Bean
    public RestClient healthRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(healthTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(healthTimeoutMs));
        return RestClient.builder().requestFactory(factory).build();
    }
}
