# COMPLETADO

# Plan: H11 + H03 — SpringAiConceptTools y log de raw content en parseLessonResponse

## Summary

Este plan cubre dos mejoras independientes de bajo alcance identificadas en la auditoría técnica.

H11 agrega una segunda Spring AI tool (`SpringAiConceptTools`) que devuelve un concepto aleatorio de Spring AI con su definición. La tool se registra en el endpoint existente `POST /api/chat/tools` junto con `DateTimeTools`, demostrando el patrón multi-tool: el modelo puede elegir entre dos herramientas según la solicitud del usuario. No se agrega ningún endpoint nuevo.

H03 mejora el debugging de `parseLessonResponse()` en `ChatService`. Cuando el modelo devuelve JSON inválido y falla la deserialización, actualmente se lanza `AiResponseFormatException` sin registrar el contenido raw que devolvió el modelo. El fix agrega un `log.warn` con el raw content antes de lanzar la excepción, lo que facilita identificar por qué el modelo produjo una respuesta malformada.

## Decisiones lockeadas

- **No se agrega endpoint nuevo**: `POST /api/chat/tools` ya expone las tools. Agregar `SpringAiConceptTools` al `.tools(...)` de `chatWithTools()` es suficiente. Crear un endpoint separado sería sobre-ingeniería.
- **Conceptos hardcodeados en la tool**: la lista de conceptos vive en `SpringAiConceptTools` como constante, sin configuración externa ni base de datos. Es una demo educativa; la simplicidad tiene prioridad.
- **Selección aleatoria con `java.util.Random`**: sin semilla fija, para que cada invocación devuelva un concepto diferente. Contrasta intencionalmente con el `seed: 42` del modelo (determinismo de texto, no de datos).
- **Raw content en H03 via `responseSpec.content()`**: para loguear el raw content hay que llamar `.content()` antes de `.entity()`. Spring AI evalúa la llamada HTTP una sola vez; se necesita obtener el texto primero y luego deserializarlo manualmente con `ObjectMapper`.
- **`ObjectMapper` inyectado en `ChatService`**: Spring Boot autoconfigura un `ObjectMapper` bean. Se inyecta por constructor, coherente con las convenciones del proyecto.
- **Tests de `SpringAiConceptTools` siguen el patrón de `DateTimeToolsTest`**: verifican que la respuesta contiene los campos esperados (nombre, definición), no el concepto exacto (aleatorio).

## Patterns a respetar

- Cadena obligatoria: `ChatController` → `ChatService` → `AiProperties` (no saltear capas)
- Nuevas tools de AI van en `ai/tools/`, anotadas con `@Tool` y registradas como `@Component`
- La tool se registra en `ChatService.chatWithTools()` via `.tools(dateTimeTools, springAiConceptTools)`
- DTOs en `chat/dto/`, nunca exponer entidades internas en la API
- Inyección por constructor, nunca `@Autowired` en campos
- Sin comentarios que expliquen qué hace el código; solo si el motivo no es obvio

## Files a crear/modificar

| Path | Acción | Razón |
|------|--------|-------|
| `src/main/java/com/example/demospringai/ai/tools/SpringAiConceptTools.java` | crear | Nueva tool `@Component` con `@Tool` que devuelve concepto aleatorio |
| `src/main/java/com/example/demospringai/chat/service/ChatService.java` | modificar | Inyectar `SpringAiConceptTools` + `ObjectMapper`; registrar en `.tools()`; refactorizar `parseLessonResponse()` para loguear raw content |
| `src/test/java/com/example/demospringai/ai/tools/SpringAiConceptToolsTest.java` | crear | Test unitario que verifica formato de la respuesta de la tool |
| `README.md` | modificar | Documentar `SpringAiConceptTools` en la sección de `POST /api/chat/tools` |

## Task list (orden de ejecución)

1. Crear `SpringAiConceptTools.java` en `ai/tools/` con lista de conceptos y método `@Tool`
2. Inyectar `SpringAiConceptTools` en `ChatService` por constructor y registrarlo en `.tools()` de `chatWithTools()`
3. Inyectar `ObjectMapper` en `ChatService` por constructor
4. Refactorizar `parseLessonResponse()`: obtener raw content con `.content()`, loguear con `log.warn`, deserializar con `ObjectMapper`
5. Crear `SpringAiConceptToolsTest.java` verificando que la respuesta contiene nombre y definición
6. Compilar: `mvn compile`
7. Ejecutar tests: `mvn test`
8. Actualizar README: sección `POST /api/chat/tools` con mención a `SpringAiConceptTools`

## Validation strategy

- [x] `mvn test` pasa sin errores
- [ ] Test manual — fecha y hora (tool existente)
- [ ] Test manual — concepto de Spring AI (tool nueva)
- [ ] Test manual — ambas tools en una conversación
- [ ] Verificar log de raw content ante respuesta inválida en `POST /api/chat/lesson`

## Out of scope

- No se agrega endpoint nuevo para la segunda tool
- No se persiste la lista de conceptos en BD ni configuración externa
- No se modifica el contrato de `ChatResponseDto` ni `LessonResponse`
- No se agrega streaming de respuestas
- No se cubren tests de integración end-to-end con Ollama real
