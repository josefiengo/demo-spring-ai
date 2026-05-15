package com.example.demospringai.ai.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class SpringAiConceptToolsTest {

    private final SpringAiConceptTools tools = new SpringAiConceptTools();

    @Test
    void conceptIncludesNameAndDefinition() {
        String result = tools.getRandomSpringAiConcept();

        assertThat(result).contains("Concepto:");
        assertThat(result).contains("Definición:");
    }

    @RepeatedTest(20)
    void conceptIsNeverNullOrBlank() {
        String result = tools.getRandomSpringAiConcept();

        assertThat(result).isNotBlank();
    }
}
