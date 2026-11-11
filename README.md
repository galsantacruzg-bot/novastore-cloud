# NovaStore Cloud · Proyecto de programación Full Stack

**Nueva actividad:** despliegue real por separado de Angular → **Vercel**, Java/Spring Boot → **Railway** y PostgreSQL → **Railway**. Código fuente editable desde Visual Studio Code. Modelo de negocio: catálogo tecnológico con clientes, carrito, pedidos de demostración y administración CRUD.

> **Estado honesto:** este paquete está preparado para desplegarse, pero las URL reales, los recursos de Railway/Vercel, el repositorio GitHub y las capturas deben crearse en las cuentas del estudiante. No se entregan URL ni evidencias inventadas. Los servicios gratuitos están sujetos a límites de recursos.

## Estructura clara

```text
NovaStore_Cloud_Vercel_Railway_CODIGO/
├── frontend/              Angular · interfaz, registro, catálogo, carrito y panel ADMIN
│   ├── src/app/           Componentes, servicios e interceptores
│   ├── scripts/           Generación de URL API para Vercel
│   └── vercel.json        Compilación y rutas SPA
├── backend/               Spring Boot · API REST, negocio, roles y persistencia
│   ├── src/main/java/     Código Java de la aplicación
│   ├── src/test/java/     Pruebas unitarias Java
│   ├── Dockerfile         Contenedor Railway
│   └── railway.json       Healthcheck y reinicio Railway
├── database/              SQL inicial y referencias de PostgreSQL
├── docs/                  Guía, informe, evidencias y modelo de negocio
├── scripts/               Comprobaciones por terminal VS Code
├── .github/workflows/     Compilación automática en GitHub Actions
├── .env.example           Variables para desarrollo local (sin claves reales)
└── package.json           Comandos principales para el desarrollo
```

## Tecnologías

- Angular 21 + TypeScript, SPA responsiva.
- Java 21 + Spring Boot 4.1.1, REST, Spring Security, Spring Data JPA.
- PostgreSQL y persistencia mediante JPA.
- Vercel (frontend), Railway (API y base de datos), GitHub (repositorio y CI).
- CORS de orígenes explícitos. API HTTPS separada, tokens `Bearer` y autorización por rol.

## Primeros comandos en VS Code

Tienes que tener Node.js 22+, Java 21, Maven y PostgreSQL para ejecución local. Abre la carpeta del proyecto en VS Code.

**Terminal A — backend:**

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/novastore"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="TU_CLAVE_LOCAL"
$env:ADMIN_EMAIL="admin@ejemplo.com"
$env:ADMIN_USERNAME="admin"
$env:ADMIN_PASSWORD="TU_CLAVE_ADMIN_SEGURA"
mvn -f backend/pom.xml spring-boot:run
```

**Terminal B — frontend:**

```powershell
cd frontend
npm install
npm start
```

URLs locales: Angular `http://localhost:4200`, API `http://localhost:8080/api/health`. El proxy del Angular dev server envía las solicitudes `/api/*` al backend. **En Vercel se configura `API_BASE_URL`, no se usa localhost**.

**Compilar y revisar:**

```powershell
npm run check:files
npm run build:web
npm run test:api
```

No se crean archivos `.bat` ni instaladores. Los comandos son de terminal de VS Code.

## Cambios frente a NovaStore 4.5.2

1. Retirados Tailscale/Funnel y el puerto 8091: Railway utiliza **solo `PORT`** y `0.0.0.0`.
2. Angular genera en build la URL de la API a partir de `API_BASE_URL` de Vercel.
3. Spring Security conserva autenticación y roles, pero **CRUD de `ADMIN` funciona desde la URL de Vercel** y no depende de VPN.
4. CORS configurado con `CORS_ALLOWED_ORIGINS` sin permitir orígenes universales.
5. Registro académico: funciona sin SMTP cuando `AUTH_REQUIRE_EMAIL_VERIFICATION=false`. **No se verifica la titularidad del correo en este modo**. Para comprobación auténtica se puede activar la API HTTPS de Resend.
6. PostgreSQL usa variables de Railway, nunca contraseñas incrustadas en el código.
7. Incluye Dockerfile multi-etapa, `railway.json`, `vercel.json`, GitHub Actions, documentación y lista de evidencias.

## Orden obligatorio de publicación

**GitHub → PostgreSQL Railway → Spring Boot Railway → Angular Vercel → CORS Railway → pruebas CRUD → informe y capturas.**

Lee `docs/GUIA_DESPLIEGUE.md` antes de iniciar. También revisa `docs/ERRORES_FRECUENTES.md`.

## Restricciones declaradas

NovaStore es una demostración académica: no procesa pagos reales. La creación pública de cuentas sin verificación de correo solo es apropiada para la práctica mientras se habilita un proveedor transaccional real. No almacenar datos sensibles reales de compradores. `spring.jpa.hibernate.ddl-auto=update` permite la demostración, pero para producción comercial hay que utilizar migraciones de esquema (Flyway/Liquibase), observabilidad y backups.
