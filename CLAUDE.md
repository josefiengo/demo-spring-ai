# CLAUDE.md

## Hard rules

- Ollama debe estar corriendo en `http://localhost:11434` antes de sugerir `mvn spring-boot:run`. Si no está up, la app falla con connection refused en el primer request de AI.
- No modificar los parámetros de sampling en `application.yml` (`seed`, `temperature`, `top-k`, `top-p`, `num-predict`) sin pedido explícito — están calibrados para determinismo (`seed: 42`, `top-k: 1`, `temperature: 0.0`).
- Toda nueva funcionalidad de AI sigue la cadena `ChatController` → `ChatService` → `AiProperties` — no llamar Spring AI directamente desde controllers ni otras capas.
- Commits siguen Conventional Commits en inglés: `feat:`, `fix:`, `chore:`, `docs:`.
- Nunca incluir línea `Co-Authored-By:` en mensajes de commit.
- Nunca hacer push directo a `main` o `master`. Todo cambio va en un branch y se integra por pull request.
- Responder siempre en español neutro: conversación, documentación, comentarios de código y cualquier texto generado. Sin regionalismos de ninguna variante. Si el usuario pide explícitamente otro idioma, usar ese idioma solo para esa respuesta.
- Nunca usar emojis en ningún contexto: respuestas, documentación ni código.
- Al generar o editar archivos `.md`, no revisar ni corregir la sintaxis Markdown después de escribirlos. Escribirlos bien desde el inicio y continuar.
- Comentarios en código solo cuando el motivo no es obvio por el nombre del método o la variable. Nunca bloques multi-línea descriptivos ni comentarios que expliquen qué hace el código.

## Arquitectura

- `chat/api/` — Controllers REST y ExceptionHandlers
- `chat/service/` — Lógica de negocio con Spring AI (`ChatClient`)
- `chat/dto/` — DTOs de request/response (nunca entidades de dominio expuestas directamente)
- `ai/tools/` — Spring AI tools: funciones Java que el modelo puede invocar (ver `DateTimeTools`)
- `config/` — Configuración de beans: `AiProperties` (custom config de Ollama), `RestClientTimeoutConfig`

## Comandos esenciales

- `docker compose up -d` — Levanta Ollama en Docker
- `docker compose exec ollama ollama pull gemma4` — Descarga el modelo si no está
- `mvn spring-boot:run` — Arranca la app en :8080 (requiere Ollama up)
- `mvn test` — Ejecuta los tests unitarios

## Convenciones de código (Java 21 + Spring Boot 3.5)

- **Records para DTOs y configuración**: usar `record` en lugar de clases con getters/setters. Ver `ChatRequest`, `AiProperties`, `LessonResponse`.
- **Inyección por constructor**: nunca usar `@Autowired` en campos. El constructor es la única forma de inyectar dependencias.
- **Interfaz + implementación en servicios**: toda lógica de negocio detrás de una interfaz (ver `ChatOperations` / `ChatService`). El controller depende de la interfaz, no de la implementación.
- **Method references sobre lambdas**: preferir `String::valueOf` sobre `s -> String.valueOf(s)` cuando son equivalentes.
- **Text blocks para strings multi-línea**: usar `"""..."""` para prompts del sistema y strings largas.
- **Pattern matching para instanceof**: usar `instanceof String text` en lugar de cast explícito.
- **Sin wildcards en imports**: cada import explícito. Nunca `import java.util.*`.
- **Logger estático con SLF4J**: `private static final Logger log = LoggerFactory.getLogger(X.class)`. Sin Lombok ni otras abstracciones.
- **`jakarta.*` no `javax.*`**: este proyecto usa Jakarta EE (Spring Boot 3.x). Nunca importar `javax.validation` ni `javax.servlet`.
- **Nuevas tools de Spring AI**: anotar con `@Tool(description = "...")` y registrar como `@Component`. Ver `DateTimeTools`.

## Principios de diseño

- **Outside-In**: diseñar desde afuera hacia adentro — primero el contrato publico (endpoint, DTOs, firma de la interfaz), luego la logica interna. El plan fija el contrato antes de tocar el servicio.
- **Tests de contrato, no de implementacion**: los tests verifican que el endpoint responde correctamente (serializacion, validacion, codigos HTTP), no como esta implementado el servicio internamente.

## Flujo de trabajo

Para cada nueva feature o fix, seguir este orden:

1. `/context <descripcion>` — cargar contexto del area afectada
2. `/plan <descripcion>` — generar plan con decisiones lockeadas antes de tocar codigo
3. `git checkout -b <tipo>/<nombre>` — crear branch (`feat/`, `fix/`, `chore/`)
4. Implementar siguiendo el plan, compilando despues de cada cambio significativo
5. Subagente `test-output-summarizer` — verificar que los tests pasan
6. `/refactor-style` — aplicar convenciones antes de commitear
7. `/segment-commits` — separar cambios en commits atomicos
8. PR a `main` — nunca push directo

Los planes completados se mueven a `.claude/plans/plan-<feature>.md` marcados como `COMPLETADO`.

## Gotchas / Dragons

- **Primera respuesta lenta**: Ollama carga el modelo en memoria al primer request (~10-30s). No es un bug. Si pasaron más de 15 min sin requests (`keep-alive: 15m`), el siguiente también tardará.
- **Configuración custom vs Spring AI**: los parámetros de Ollama están en `app.ai.ollama.*` (leídos por `AiProperties`), NO en `spring.ai.ollama.*`. Agregar config en el namespace de Spring AI directamente no será tomada.
- **UTF-8 en Windows**: curl en Git Bash puede no enviar correctamente caracteres acentuados. Usar `printf '%s' '...' | curl ... --data-binary @-` o PowerShell con `[System.Text.Encoding]::UTF8.GetBytes($body)`.
