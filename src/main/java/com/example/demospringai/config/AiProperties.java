package com.example.demospringai.config;

import java.time.Duration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.ai")
@Validated
public record AiProperties(
        @Valid
        @NotNull
        Http http,

        @Valid
        @NotNull
        Ollama ollama
) {

    public record Http(
            @NotNull
            Duration connectTimeout,

            @NotNull
            Duration readTimeout
    ) {
    }

    public record Ollama(
            @NotBlank
            String model,

            @DecimalMin("0.0")
            @DecimalMax("2.0")
            double temperature,

            int seed,

            @Min(1)
            int topK,

            @DecimalMin("0.0")
            @DecimalMax("1.0")
            double topP,

            @Min(1)
            int numCtx,

            @Min(1)
            int numPredict,

            @NotBlank
            String keepAlive
    ) {
    }
}
