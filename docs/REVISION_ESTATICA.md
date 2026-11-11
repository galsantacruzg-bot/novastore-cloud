# Auditoría de código fuente (realizada en este entorno)

APROBADO | JSON válido
APROBADO | PORT Railway
APROBADO | DB por entorno
APROBADO | CORS por entorno
APROBADO | Servidor sin Funnel
APROBADO | Dockerfile
APROBADO | Vercel config
APROBADO | Login y registro
APROBADO | ADMIN roles
APROBADO | GitHub CI
APROBADO | DOCX incluido
APROBADO | Sin .env.local
APROBADO | Sin instaladores
APROBADO | Imports y plantillas Angular | 32 TS; 0 rutas rotas
APROBADO | Java parser | Java sintaxis: 44 archivos, errores 0
APROBADO | TypeScript parser | 32 TypeScript, 0 errores
APROBADO | API local build config
APROBADO | API Railway build config
APROBADO | Vercel requiere URL
APROBADO | Pruebas Java 4 archivos
APROBADO | Sin claves privadas obvias

**Limitación:** no fue posible ejecutar `mvn test package` ni `npm install`/`ng build` porque este entorno no tiene acceso a los repositorios de dependencias. Las comprobaciones de sintaxis no equivalen a una compilación integrada. GitHub Actions incluye dichas tareas para verificarlas en un entorno con Internet. Tampoco se ha creado una URL pública en Railway/Vercel.
