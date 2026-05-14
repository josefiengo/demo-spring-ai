# Demo Spring AI

Proyecto base para aprender Spring Boot 3.5 con Spring AI 1.1 usando Ollama como modelo local.

## Requisitos

- Java 21
- Maven 3.9+
- Ollama (nativo o Docker)

## Quick start

**Con Ollama nativo:**

```bash
ollama pull gemma4:latest
mvn spring-boot:run
```

**Con Docker:**

```bash
docker compose up -d
docker compose exec ollama ollama pull gemma4:latest
mvn spring-boot:run
```

La primera respuesta puede tardar hasta 30 segundos mientras Ollama carga el modelo en memoria.

## Endpoints

Base URL local: `http://localhost:8080`

| Método | Path | Descripción |
|--------|------|-------------|
| `GET` | `/api/health` | Verifica que la app está corriendo |
| `GET` | `/api/chat?message=` | Chat vía query param |
| `POST` | `/api/chat` | Chat vía body JSON |
| `POST` | `/api/chat/lesson` | Respuesta estructurada como JSON |
| `POST` | `/api/chat/tools` | Chat con acceso a herramientas Java (fecha y hora) |
| `GET` | `/api/chat/history` | Historial de intercambios de la sesión actual |

### `GET /api/health`

Comprueba que la aplicación responde. No llama al modelo de IA.

**Request**

```bash
curl http://localhost:8080/api/health
```

**Response `200 OK`**

```json
{
  "answer": "Spring Boot está listo. Configurar Ollama con gemma4 para usar /api/chat."
}
```

### `GET /api/chat?message=...`

Envía un mensaje al modelo usando un query param.

**Query params**

| Nombre | Tipo | Requerido | Descripción |
|--------|------|-----------|-------------|
| `message` | `string` | Sí | Prompt o pregunta para el modelo |

**Request**

```bash
curl "http://localhost:8080/api/chat?message=Que%20es%20Spring%20AI%3F"
```

**Response `200 OK`**

```json
{
  "answer": "Respuesta generada por el modelo..."
}
```

Este endpoint guarda el intercambio en `/api/chat/history`.

### `POST /api/chat`

Envía un mensaje al modelo usando un body JSON.

**Body**

```json
{
  "message": "Qué es Spring AI?"
}
```

**Campos**

| Nombre | Tipo | Requerido | Validación |
|--------|------|-----------|------------|
| `message` | `string` | Sí | No puede estar vacío |

**Request**

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Qué es Spring AI?"}'
```

**Response `200 OK`**

```json
{
  "answer": "Respuesta generada por el modelo..."
}
```

Este endpoint guarda el intercambio en `/api/chat/history`.

### `POST /api/chat/lesson`

Pide una lección breve y devuelve una respuesta estructurada. Este endpoint usa un `system` prompt mínimo para indicar que la respuesta debe ser una lección en español.

**Body**

```json
{
  "message": "Explícame qué es un ChatClient en Spring AI"
}
```

**Response `200 OK`**

```json
{
  "topic": "ChatClient en Spring AI",
  "explanation": "ChatClient es la API de alto nivel para enviar prompts a un modelo de chat...",
  "keyIdeas": [
    "Permite configurar mensajes system y user",
    "Puede devolver texto o mapear la respuesta a un DTO",
    "Centraliza opciones del modelo y llamadas a herramientas"
  ],
  "nextExercise": "Crear un endpoint que use ChatClient para resumir un texto corto."
}
```

Este endpoint guarda el intercambio en `/api/chat/history`.

### `POST /api/chat/tools`

Envía un mensaje al modelo con acceso a herramientas Java registradas en Spring AI.
Actualmente expone `DateTimeTools.getCurrentDateTime()`, que permite al modelo consultar la fecha y hora actuales.
Este endpoint usa un `system` prompt mínimo para indicar que puede usar herramientas cuando necesite datos actuales disponibles.

**Body**

```json
{
  "message": "Qué fecha y hora es ahora?"
}
```

**Response `200 OK`**

```json
{
  "answer": "La fecha y hora actual es 2026-05-14T18:20:00-04:00..."
}
```

Este endpoint guarda el intercambio en `/api/chat/history`.

### `GET /api/chat/history`

Devuelve los intercambios registrados en memoria durante la ejecución actual de la aplicación.
Al reiniciar la aplicación, el historial vuelve a empezar vacío.

**Request**

```bash
curl http://localhost:8080/api/chat/history
```

**Response `200 OK`**

```json
[
  {
    "endpoint": "POST /api/chat",
    "message": "Qué es Spring AI?",
    "response": "Spring AI es un proyecto de Spring...",
    "timestamp": "2026-05-14T22:20:00Z"
  }
]
```

### Errores

Los endpoints `POST /api/chat`, `POST /api/chat/lesson` y `POST /api/chat/tools` validan el body JSON.

**JSON inválido: `400 Bad Request`**

```json
{
  "code": "INVALID_JSON",
  "message": "El body debe ser JSON válido en UTF-8 con el formato: {\"message\":\"Hola\"}"
}
```

**Mensaje vacío: `400 Bad Request`**

```json
{
  "code": "INVALID_REQUEST",
  "message": "El campo message es obligatorio"
}
```

**Timeout de Ollama: `504 Gateway Timeout`**

```json
{
  "code": "AI_TIMEOUT",
  "message": "Ollama no respondió a tiempo. Verificar que el modelo esté cargado o probar con un modelo más pequeño."
}
```

**Respuesta estructurada inválida: `502 Bad Gateway`**

```json
{
  "code": "AI_INVALID_RESPONSE",
  "message": "El modelo devolvió una respuesta que no cumple el formato esperado. Reintentar la solicitud."
}
```

**Error al llamar al modelo: `502 Bad Gateway`**

```json
{
  "code": "AI_UNAVAILABLE",
  "message": "No se pudo obtener respuesta del modelo de IA. Revisar Ollama y el modelo configurado."
}
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
