---
name: refactor-style
description: Aplica las convenciones de código del proyecto a uno o más archivos Java. Si no se especifica un archivo, usa el subagent style-auditor para auditar todo el proyecto primero y luego aplica los cambios archivo por archivo.
argument-hint: <ruta-archivo.java | --all>
---

Vas a refactorizar para aplicar las convenciones del proyecto a:

$ARGUMENTS

## Prerequisito

Lee `CLAUDE.md` completo, sección "Convenciones de código". Esas son las reglas que vas a aplicar.

## Procedimiento

### Fase 1: Obtener lista de archivos a refactorizar

**Si el argumento es un archivo específico** (`src/main/java/.../Archivo.java`):
- Trabajar solo sobre ese archivo.

**Si el argumento es `--all` o no se especifica archivo**:
- Invocar el subagent `style-auditor` para obtener el reporte de violaciones.
- Trabajar solo sobre los archivos que el subagent reportó con violaciones. No tocar los limpios.

### Fase 2: Aplicar convenciones por archivo

Para cada archivo con violaciones, aplicar en este orden:

1. **Reemplazar `javax.*` por `jakarta.*`** en imports
2. **Eliminar wildcard imports** — expandir a imports explícitos
3. **Eliminar `@Autowired`** en campos — convertir a inyección por constructor
4. **Reemplazar lambdas por method references** cuando el cuerpo es una sola llamada a método
5. **Aplicar pattern matching para instanceof** — `(X) obj` → `obj instanceof X x`
6. **Verificar Logger** — debe ser `private static final Logger log = LoggerFactory.getLogger(X.class)`

### Fase 3: Verificar compilación

Después de cada archivo modificado, ejecutar:

```bash
mvn compile -q 2>&1
```

Si falla, revertir solo el último cambio y reportar el error. No continuar con el siguiente archivo hasta resolver.

### Fase 4: Reporte final

```
Refactor completado:

Archivos modificados:
- [archivo] — [lista de cambios aplicados]

Archivos sin cambios necesarios:
- [archivo]

Errores (si los hay):
- [archivo:línea] — [descripción del problema no resuelto]
```

## Reglas estrictas

- **No cambiar lógica de negocio**. Solo estilo y convenciones.
- **No agregar ni eliminar métodos**. Solo transformar los existentes.
- **Compilar después de cada archivo**. No acumular cambios sin validar.
- **Si hay duda sobre si un lambda es reemplazable**, dejarlo como está y reportarlo.
