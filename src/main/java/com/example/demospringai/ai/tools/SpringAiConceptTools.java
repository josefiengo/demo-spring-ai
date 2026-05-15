package com.example.demospringai.ai.tools;

import java.util.List;
import java.util.Random;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class SpringAiConceptTools {

    private static final List<String> CONCEPTS = List.of(
            """
            Concepto: ChatClient
            Definición: API fluida de Spring AI para construir y enviar prompts a un modelo de chat. \
            Permite encadenar system prompt, user message, tools y opciones de modelo en una sola expresión.
            """,
            """
            Concepto: Tool (herramienta)
            Definición: Función Java que el modelo puede invocar durante una respuesta. \
            Se declara con @Tool y Spring AI se encarga de describírsela al modelo y ejecutarla cuando la solicita.
            """,
            """
            Concepto: StructuredOutput
            Definición: Capacidad de Spring AI para mapear la respuesta del modelo directamente a un objeto Java (record o clase). \
            Se activa llamando a .entity(MiClase.class) en el ChatClient.
            """,
            """
            Concepto: ChatOptions
            Definición: Conjunto de parámetros que controlan el comportamiento del modelo: temperatura, seed, top-k, top-p, num-predict. \
            En este proyecto se configuran vía OllamaChatOptions desde AiProperties.
            """,
            """
            Concepto: SystemPrompt
            Definición: Mensaje de contexto enviado al modelo antes del mensaje del usuario. \
            Define el rol, las restricciones de formato y el tono de la respuesta sin que el usuario lo vea.
            """
    );

    private final Random random = new Random();

    @Tool(description = "Devuelve un concepto aleatorio de Spring AI con su definición en español")
    public String getRandomSpringAiConcept() {
        return CONCEPTS.get(random.nextInt(CONCEPTS.size()));
    }
}
