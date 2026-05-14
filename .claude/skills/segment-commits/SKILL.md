---
name: segment-commits
description: Use when the user has multiple unrelated or loosely related changes and wants to split them into atomic, coherent commits instead of committing everything together. Triggers when the user says "segmenta los commits", "cómo divido estos cambios", "quiero commits atómicos", "separa los cambios en commits", or when there are staged or unstaged changes that mix different concerns. Does NOT run git commands — only proposes the segmentation for the user to execute.
---

# Skill: Segment Commits

Analiza los cambios pendientes y propone cómo dividirlos en commits atómicos y coherentes.

## Fase 1: Leer el estado actual

Ejecuta en orden:

```bash
git diff --staged --name-only
git diff --name-only
git status --short
```

Si no hay ningún cambio (ni staged ni unstaged), avisa al usuario y detente.

## Fase 2: Agrupar por responsabilidad

Para cada archivo modificado, determina a qué responsabilidad pertenece. Usa estas categorías como guía (no son exhaustivas):

| Categoría | Archivos típicos en este repo |
|-----------|-------------------------------|
| `feat(chat)` | `ChatController`, `ChatService`, `ChatOperations`, DTOs |
| `feat(ai)` | `ai/tools/`, nuevas tools de Spring AI |
| `chore(config)` | `application.yml`, `AiProperties`, `RestClientTimeoutConfig` |
| `chore(deps)` | `pom.xml` |
| `chore(docker)` | `compose.yaml` |
| `docs` | `README.md`, `CLAUDE.md`, comentarios de documentación |
| `test` | archivos en `src/test/` |
| `chore(ai-layer)` | `.claude/` |

Si un archivo pertenece a más de una categoría, agrúpalo con el cambio más representativo y anótalo.

## Fase 3: Proponer segmentación

Presenta la propuesta en este formato:

```
Propuesta de segmentación:

Commit 1 — feat(chat): <descripción>
  Archivos:
  - src/main/java/.../ChatController.java
  - src/main/java/.../ChatService.java
  Comando:
  git add <archivos> && git commit -m "feat(chat): <descripción>"

Commit 2 — chore(config): <descripción>
  Archivos:
  - src/main/resources/application.yml
  Comando:
  git add <archivos> && git commit -m "chore(config): <descripción>"
```

Incluye el comando exacto para cada commit para que el usuario pueda ejecutarlos directamente.

## Fase 4: Advertir sobre dependencias

Si detectas que un grupo de archivos depende de otro para compilar o funcionar correctamente, advierte el orden de ejecución:

```
Orden recomendado: Commit 2 antes que Commit 1 — ChatService depende de AiProperties.
```

## Reglas estrictas

- **NO ejecutes ningún comando git**. Solo propones, el usuario ejecuta.
- **NO generes más de 6 commits**. Si hay más de 6 grupos naturales, consolida los menores.
- Si todos los cambios pertenecen a la misma responsabilidad, indicarlo: un solo commit es la respuesta correcta.
- Los mensajes propuestos siguen Conventional Commits en inglés, sin `Co-Authored-By:`.
