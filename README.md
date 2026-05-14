# Demo Spring AI

Proyecto base para aprender Spring Boot con Spring AI.

## Requisitos

- Java 21
- Maven 3.9+
- Ollama local para el perfil predeterminado

Spring AI 1.1.x soporta Spring Boot 3.4.x y 3.5.x. Este proyecto usa Spring Boot 3.5.14 y Spring AI 1.1.6.

## Ejecución con Ollama

Instalar Ollama, descargar un modelo y ejecutar la aplicación:

```bash
ollama pull gemma4
mvn spring-boot:run
```

Ollama también puede ejecutarse con Docker:

```bash
docker compose up -d
docker compose exec ollama ollama pull gemma4
mvn spring-boot:run
```

Probar el chat desde Bash:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json; charset=utf-8" \
  --data-raw '{"message":"Explícame qué es Spring AI en 5 líneas"}'
```

Probar el chat desde PowerShell:

```powershell
$body = @{ message = "Explícame qué es Spring AI en 5 líneas" } | ConvertTo-Json
$utf8Body = [System.Text.Encoding]::UTF8.GetBytes($body)

Invoke-RestMethod `
  -Uri http://localhost:8080/api/chat `
  -Method Post `
  -ContentType "application/json; charset=utf-8" `
  -Body $utf8Body
```

Si Git Bash en Windows no envía correctamente caracteres acentuados, enviar el JSON por stdin:

```bash
printf '%s' '{"message":"Explícame qué es Spring AI en 5 líneas"}' \
  | curl -X POST http://localhost:8080/api/chat \
      -H "Content-Type: application/json; charset=utf-8" \
      --data-binary @-
```

La primera respuesta con Ollama puede tardar porque el modelo se carga en memoria. Si la latencia es alta, se puede probar un modelo más pequeño y cambiar `app.ai.ollama.model` en `application.yml`.
También se pueden ajustar `app.ai.ollama.seed`, `top-k`, `top-p`, `num-predict` y `keep-alive` para balancear determinismo, longitud de respuesta, latencia y memoria.

### Parámetros Locales de Ollama

Estos valores están en `application.yml`, dentro de `app.ai.ollama`:

```yaml
app:
  ai:
    ollama:
      seed: 42
      top-k: 1
      top-p: 1.0
      num-ctx: 2048
      num-predict: 240
      keep-alive: 15m
```

- `seed`: fija la semilla de generación. Ayuda a obtener respuestas más repetibles cuando el resto de los parámetros también es determinista.
- `top-k`: limita cuántos tokens candidatos considera el modelo en cada paso. Con `1`, elige el candidato más probable y reduce la variación.
- `top-p`: limita candidatos por probabilidad acumulada. `1.0` no recorta por núcleo; valores menores, como `0.8`, hacen que la salida sea más conservadora.
- `num-ctx`: define el tamaño de contexto que el modelo puede usar entre prompt, instrucciones e historial. Un contexto mayor consume más memoria.
- `num-predict`: define el máximo de tokens que el modelo puede generar. Valores altos permiten respuestas largas, pero aumentan la latencia.
- `keep-alive`: define cuánto tiempo Ollama mantiene el modelo cargado en memoria después de responder. Valores altos aceleran llamadas siguientes, pero retienen memoria.

## Endpoints

- `GET /api/health`: prueba básica sin IA.
- `GET /api/chat?message=`: chat normal vía query param.
- `POST /api/chat`: chat normal vía body JSON.
- `POST /api/chat/lesson`: devuelve una respuesta estructurada como JSON.
- `POST /api/chat/tools`: permite al modelo usar una herramienta Java para consultar la fecha y hora actuales.

## Estructura

```text
src/main/java/com/example/demospringai
├── DemoSpringAiApplication.java
├── ai
│   └── tools
│       └── DateTimeTools.java
├── chat
│   ├── api
│   │   ├── ChatController.java
│   │   └── ChatExceptionHandler.java
│   ├── dto
│   │   ├── ChatRequest.java
│   │   ├── ChatResponseDto.java
│   │   ├── ErrorResponseDto.java
│   │   └── LessonResponse.java
│   └── service
│       ├── ChatOperations.java
│       └── ChatService.java
└── config
    ├── AiProperties.java
    └── RestClientTimeoutConfig.java
src/main/resources
└── application.yml
```
