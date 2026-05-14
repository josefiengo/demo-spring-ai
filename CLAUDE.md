# CLAUDE.md

## Hard rules

- Ollama debe estar corriendo en `http://localhost:11434` antes de sugerir `mvn spring-boot:run`. Si no está up, la app falla con connection refused en el primer request de AI.
- No modificar los parámetros de sampling en `application.yml` (`seed`, `temperature`, `top-k`, `top-p`, `num-predict`) sin pedido explícito — están calibrados para determinismo (`seed: 42`, `top-k: 1`, `temperature: 0.0`).
- Toda nueva funcionalidad de AI sigue la cadena `ChatController` → `ChatService` → `AiProperties` — no llamar Spring AI directamente desde controllers ni otras capas.
- Commits siguen Conventional Commits en inglés: `feat:`, `fix:`, `chore:`, `docs:`.
- Nunca incluir línea `Co-Authored-By:` en mensajes de commit.

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

## Gotchas / Dragons

- **Primera respuesta lenta**: Ollama carga el modelo en memoria al primer request (~10-30s). No es un bug. Si pasaron más de 15 min sin requests (`keep-alive: 15m`), el siguiente también tardará.
- **Configuración custom vs Spring AI**: los parámetros de Ollama están en `app.ai.ollama.*` (leídos por `AiProperties`), NO en `spring.ai.ollama.*`. Agregar config en el namespace de Spring AI directamente no será tomada.
- **UTF-8 en Windows**: curl en Git Bash puede no enviar correctamente caracteres acentuados. Usar `printf '%s' '...' | curl ... --data-binary @-` o PowerShell con `[System.Text.Encoding]::UTF8.GetBytes($body)`.
