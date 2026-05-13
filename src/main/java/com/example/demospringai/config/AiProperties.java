package com.example.demospringai.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        Http http,
        Ollama ollama
) {

    public record Http(
            Duration connectTimeout,
            Duration readTimeout
    ) {
    }

    public record Ollama(
            String model,
            double temperature,
            int seed,
            int topK,
            double topP,
            int numCtx,
            int numPredict,
            String keepAlive
    ) {
    }
}
