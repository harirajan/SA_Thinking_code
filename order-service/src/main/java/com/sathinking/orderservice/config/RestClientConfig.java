package com.sathinking.orderservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Primary
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @LoadBalanced
    @Bean
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }
}