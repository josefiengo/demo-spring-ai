# Demo Spring AI

Proyecto base para aprender Spring Boot 3.5 con Spring AI 1.1 usando Ollama como modelo local.

## Requisitos

- Java 21
- Maven 3.9+
- Ollama (nativo o Docker)

## Quick start

**Con Ollama nativo:**

```bash
ollama pull gemma4
mvn spring-boot:run
```

**Con Docker:**

```bash
docker compose up -d
docker compose exec ollama ollama pull gemma4
mvn spring-boot:run
```

La primera respuesta puede tardar hasta 30 segundos mientras Ollama carga el modelo en memoria.

## Endpoints

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/health` | Verifica que la app está corriendo |
| `GET` | `/api/chat?message=` | Chat vía query param |
| `POST` | `/api/chat` | Chat vía body JSON |
| `POST` | `/api/chat/lesson` | Respuesta estructurada como JSON |
| `POST` | `/api/chat/tools` | Chat con acceso a herramientas Java (fecha y hora) |
| `GET` | `/api/chat/history` | Historial de intercambios de la sesión actual |

Ejemplo:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Qué es Spring AI?"}'
```

## Estructura

```text
src/main/java/com/example/demospringai
├── ai/tools/          — Spring AI tools (funciones Java invocables por el modelo)
├── chat/api/          — Controllers REST y manejo de excepciones
├── chat/dto/          — DTOs de request y response
├── chat/service/      — Lógica de negocio con ChatClient
└── config/            — AiProperties y configuración de beans
```

La configuración de Ollama (modelo, temperatura, contexto, etc.) vive en `src/main/resources/application.yml` bajo `app.ai.ollama`.
