---
name: context
description: Carga contexto del codebase desde la lente de un ticket o descripción de feature, sin escribir código.
argument-hint: <github-issue-number-or-description>
---

Carga contexto para trabajar en:

$ARGUMENTS

## Procedimiento (orden estricto)

### 1. Identidad del repo

Lee `CLAUDE.md` completo. Si no existe, avísame y detente.

### 2. Actividad reciente

Ejecuta:

```bash
git log --oneline -20
```

Nota qué áreas están en desarrollo activo y qué tipo de commits dominan.

### 3. Si hay número de issue (#NN)

Si el argumento es un número de issue de GitHub:

- Usa el MCP de GitHub si está disponible para leer el issue completo
- Si no hay MCP, pídeme que pegue el contenido del issue

### 4. Archivos relevantes

En base al issue o descripción, identifica 5-10 archivos que probablemente se tocan. Sigue este orden de búsqueda:

1. `ChatController` / `ChatExceptionHandler` — si afecta endpoints REST
2. `ChatService` / `ChatOperations` — si afecta lógica de AI o prompts
3. `ai/tools/` — si implica agregar una nueva tool que el modelo puede invocar
4. `config/AiProperties` / `application.yml` — si afecta parámetros de Ollama
5. `chat/dto/` — si cambia el contrato request/response

NO leas todos los archivos. Solo lista los paths con 1 línea de por qué.

### 5. Summary

Devuelve máximo 200 palabras con:

- Capa(s) del repo afectadas (`api`, `service`, `dto`, `tools`, `config`)
- Archivos identificados (con 1 línea cada uno)
- Actividad reciente relevante (commits cercanos a los archivos identificados)
- 1 pregunta crítica que necesito aclarar antes de planificar

## Reglas estrictas

- **NO escribas código** en esta fase.
- **NO empieces a planificar** la implementación.
- **NO leas más de 5 archivos completos** — prefiere grep + lectura puntual.
- Si falta información crítica, **pregunta**, no inventes.
