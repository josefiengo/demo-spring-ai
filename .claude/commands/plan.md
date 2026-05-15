---
name: plan
description: Genera plan estructurado para implementar una feature, después de tener contexto cargado con /context.
argument-hint: <feature-description>
---

Genera un plan de implementación estructurado para:

$ARGUMENTS

## Prerequisito

Asume que ya ejecutaste `/context` en esta sesión. Si no, ejecuta `/context $ARGUMENTS` primero y vuelve.

## Procedimiento

### Fase 1: Decisiones a lockear

Antes de escribir el plan, identifica decisiones no triviales:

- ¿La feature requiere un nuevo endpoint o modifica uno existente?
- ¿Necesita una nueva Spring AI tool (función Java invocable por el modelo) o alcanza con prompt engineering?
- ¿Cambia el contrato request/response? Si es así, hay que actualizar DTOs y tests.

Si hay ambigüedad importante, **pregunta antes de continuar**.

### Fase 2: Generar el plan

Escribe el archivo directamente en `.claude/plans/plan-<feature>.md` (nunca en la raíz del repo). Deriva `<feature>` del argumento: kebab-case, conciso. Estructura obligatoria:

```markdown
# Plan: <feature-name>

## Summary
[2-3 párrafos: qué se va a construir y por qué]

## Decisiones lockeadas
- [decisión 1 + razón]
- [decisión 2 + razón]

## Patterns a respetar
- Cadena obligatoria: `ChatController` → `ChatService` → `AiProperties` (no saltear capas)
- Nuevas tools de AI van en `ai/tools/`, anotadas con `@Bean` y registradas en `ChatService`
- DTOs en `chat/dto/`, nunca exponer entidades internas en la API
- Configuración de Ollama solo via `application.yml` bajo `app.ai.ollama.*`

## Files a crear/modificar
| Path | Acción | Razón |
|------|--------|-------|
| src/main/java/com/example/demospringai/... | create/modify | ... |

## Task list (orden de ejecución)
1. [task atómico 1]
2. [task atómico 2]
...

## Validation strategy
- [ ] `mvn test` pasa sin errores
- [ ] Endpoint responde correctamente con: `curl -X POST http://localhost:8080/api/...`
- [ ] Ollama corriendo antes de probar (`docker compose up -d`)
- [ ] Manual test: [escenario específico con el request de prueba]

## Out of scope
- [qué intencionalmente NO se hace en este plan]
```

### Fase 3: Validar el plan generado

Revísalo críticamente:

- ¿Hay tasks > 1 hora? Divídelos.
- ¿La validation strategy incluye el curl de prueba concreto?
- ¿Los paths de archivos siguen la estructura `com/example/demospringai/`?

Si algo no pasa la revisión, regenera esa sección.

## Reglas estrictas

- **NO implementes código** aquí. Solo plan.
- **NO crees branches git**. Eso es trabajo posterior.
- Si el feature tiene >10 tasks, sugiere dividirlo en 2 features.
