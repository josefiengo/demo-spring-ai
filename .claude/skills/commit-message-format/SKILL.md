---
name: commit-message-format
description: Use when the user is about to commit changes or asks for a commit message. Triggers when the user says "hacer commit", "necesito un commit message", "genera el mensaje de commit", "qué mensaje le pongo al commit", or when staged changes are ready and the user asks what to write. Generates a Conventional Commits message in English based on the staged diff. Does NOT run git commit — only produces the message for the user to review and use.
---

# Skill: Commit Message Format

Genera un mensaje de commit siguiendo Conventional Commits basado en los cambios en staging.

## Fase 1: Leer los cambios

Ejecuta:

```bash
git diff --staged
```

Si no hay nada en staging, avisa al usuario y detente. No generes un mensaje de cambios sin ver qué hay.

## Fase 2: Clasificar el tipo

Elige el tipo según los cambios:

| Tipo | Cuándo usarlo |
|------|---------------|
| `feat` | Se agrega funcionalidad nueva (endpoint, tool, servicio) |
| `fix` | Se corrige un bug |
| `chore` | Cambios de configuración, dependencias, build (`pom.xml`, `application.yml`, `compose.yaml`) |
| `docs` | Solo documentación (`README.md`, `CLAUDE.md`, comentarios) |
| `refactor` | Reorganización sin cambio de comportamiento |
| `test` | Agrega o modifica tests |

## Fase 3: Construir el mensaje

Formato obligatorio:

```
<tipo>(<scope>): <descripción en inglés, imperativo, máx 72 chars>
```

Scopes válidos para este repo:

- `chat` — ChatController, ChatService, DTOs
- `ai` — tools de Spring AI (DateTimeTools y futuros)
- `config` — AiProperties, RestClientTimeoutConfig, application.yml
- `deps` — cambios en pom.xml
- `docker` — compose.yaml
- `docs` — documentación

Ejemplos correctos:

```
feat(chat): add conversation history endpoint
fix(config): set correct keep-alive default for Ollama
chore(deps): upgrade spring-ai to 1.1.7
test(chat): add missing validation test for empty message
```

## Fase 4: Presentar al usuario

Muestra:
1. El mensaje generado en un bloque de código para fácil copiado
2. Una línea explicando por qué elegiste ese tipo y scope
3. Si hay ambigüedad entre dos tipos, presenta las dos opciones con el trade-off

No ejecutes `git commit`. El usuario decide cuándo y cómo commitearlo.
