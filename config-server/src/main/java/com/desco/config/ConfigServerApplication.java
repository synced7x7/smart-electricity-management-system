package com.desco.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}


/*DESCO Simulation - Config Server
Central configuration server for all microservices.
Serves configuration from the classpath configs/ directory.
Protected by HTTP Basic Auth.*/
