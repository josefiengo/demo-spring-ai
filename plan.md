# Plan: agregar endpoint de historial

## Summary

Se agrega un endpoint `GET /api/chat/history` que devuelve la lista de todos los intercambios
realizados desde que la aplicacion levanto. Cada entrada registra el endpoint invocado, el mensaje
del usuario, la respuesta del modelo y el timestamp de la llamada.

El almacenamiento es en memoria (JVM): una lista thread-safe dentro de `ChatService`. No se agrega
ninguna dependencia externa ni base de datos. El historial se pierde al reiniciar la app, lo cual
es aceptable para el alcance de este plan.

La feature sigue la cadena obligatoria del repo: el nuevo endpoint en `ChatController` delega a un
metodo nuevo en `ChatOperations`, implementado en `ChatService`. Se agrega un DTO dedicado para no
exponer estructuras internas en la API.

## Decisiones lockeadas

- **Almacenamiento en memoria** — `CopyOnWriteArrayList` en `ChatService` para thread-safety sin
  sincronizacion explicita. Razon: la carga de escritura es baja (un insert por request de AI) y la
  lectura es el caso predominante.
- **Registro en `loggedAiCall`** — el metodo ya centraliza toda llamada al modelo; es el unico
  lugar donde agregar el registro sin duplicar logica. Razon: evita modificar los tres metodos
  publicos por separado.
- **DTO separado `HistoryEntryDto`** — no reusar `ChatResponseDto`. Razon: el historial tiene
  campos distintos (endpoint, timestamp, message) que no pertenecen al DTO de respuesta simple.
- **Timestamp como `Instant`** — serializado como ISO-8601 por Jackson por defecto. Razon: formato
  estandar, sin dependencias adicionales.

## Patterns a respetar

- Cadena obligatoria: `ChatController` → `ChatService` → `AiProperties` (no saltear capas)
- Nuevas tools de AI van en `ai/tools/`, anotadas con `@Bean` y registradas en `ChatService`
- DTOs en `chat/dto/`, nunca exponer entidades internas en la API
- Configuracion de Ollama solo via `application.yml` bajo `app.ai.ollama.*`

## Files a crear/modificar

| Path | Accion | Razon |
|------|--------|-------|
| `src/main/java/com/example/demospringai/chat/dto/HistoryEntryDto.java` | crear | DTO que representa una entrada del historial (endpoint, message, response, timestamp) |
| `src/main/java/com/example/demospringai/chat/service/ChatOperations.java` | modificar | Agregar metodo `List<HistoryEntryDto> history()` a la interfaz |
| `src/main/java/com/example/demospringai/chat/service/ChatService.java` | modificar | Agregar `CopyOnWriteArrayList`, poblar en `loggedAiCall`, implementar `history()` |
| `src/main/java/com/example/demospringai/chat/api/ChatController.java` | modificar | Agregar `GET /api/chat/history` que delega a `chatOperations.history()` |
| `src/test/java/com/example/demospringai/chat/api/ChatControllerTest.java` | modificar | Agregar test para `GET /api/chat/history` (lista vacia y con entradas) |
| `README.md` | modificar | Agregar `GET /api/chat/history` a la seccion de Endpoints |

## Task list (orden de ejecucion)

1. Crear `HistoryEntryDto` como record con campos: `String endpoint`, `String message`, `String response`, `Instant timestamp`
2. Agregar `List<HistoryEntryDto> history()` a la interfaz `ChatOperations`
3. En `ChatService`, agregar campo `List<HistoryEntryDto> history = new CopyOnWriteArrayList<>()`
4. En `ChatService.loggedAiCall`, agregar al historial despues de obtener la respuesta exitosa
5. Implementar `history()` en `ChatService` devolviendo `Collections.unmodifiableList(history)`
6. Agregar `GET /api/chat/history` en `ChatController` que retorna `List<HistoryEntryDto>`
7. Agregar tests en `ChatControllerTest`: historial vacio al inicio, historial con una entrada tras un POST
8. Actualizar `README.md`: agregar `GET /api/chat/history` a la seccion de Endpoints

## Validation strategy

- [ ] `mvn test` pasa sin errores
- [ ] Historial vacio antes de cualquier llamada:
  ```bash
  curl http://localhost:8080/api/chat/history
  ```
  Respuesta esperada: `[]`
- [ ] Historial con una entrada despues de un chat:
  ```bash
  curl -X POST http://localhost:8080/api/chat \
    -H "Content-Type: application/json" \
    --data-raw '{"message":"Hola"}'

  curl http://localhost:8080/api/chat/history
  ```
  Respuesta esperada: lista con un objeto que tiene `endpoint`, `message`, `response`, `timestamp`
- [ ] Ollama corriendo antes de probar (`docker compose up -d`)

## Out of scope

- Persistencia en base de datos
- Paginacion del historial
- Filtrado por endpoint o fecha
- Borrar o limpiar el historial via API
- Historial por sesion de usuario (no hay autenticacion)
- Registrar llamadas fallidas (solo se guardan respuestas exitosas)
