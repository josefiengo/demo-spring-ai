package com.example.demospringai.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RestClientTimeoutConfig {

    @Bean
    RestClientCustomizer aiRestClientTimeouts(AiProperties aiProperties) {
        AiProperties.Http http = aiProperties.http();
        var settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(http.connectTimeout())
                .withReadTimeout(http.readTimeout());

        return builder -> builder.requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings));
    }
}
