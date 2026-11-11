# Guía completa para la P.A.E. Vercel + Railway

## 0. Antes de empezar

Este trabajo **no usa Tailscale, Funnel, IP 100.x ni tu computadora como servidor público**. Son servicios independientes conectados por HTTPS. No subas `.env.local`, contraseñas, credenciales SMTP ni capturas con claves a GitHub.

**Fecha de la actividad mostrada en el aula:** apertura 06/10/2026 y cierre 09/11/2026 23:00. Consulta con el docente si aún admite entrega.

## 1. Verificación local desde VS Code

1. PostgreSQL local creado con base `novastore` (usa `database/01_CREAR_BASE_EN_PGADMIN.sql` si corresponde).
2. Terminal backend: define `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, luego `mvn -f backend/pom.xml spring-boot:run`.
3. Terminal frontend: `cd frontend`, `npm install`, `npm start`.
4. Prueba `http://localhost:8080/api/health` y `http://localhost:4200`.
5. Registrar cliente de prueba, iniciar sesión, entrar con el administrador, ejecutar CRUD de productos y clientes; no usar datos personales reales para la demostración.

## 2. Subir código a GitHub

En `https://github.com/new` crea el repositorio **novastore-cloud** (vacío, sin README adicional), público o privado con integración autorizada.

Desde la raíz en VS Code:

```powershell
git init
git add .
git status
git commit -m "NovaStore Cloud: Angular Vercel, Spring Boot Railway, PostgreSQL"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/novastore-cloud.git
git push -u origin main
```

`git status` no debe mostrar `.env.local`, `node_modules`, `target` ni credenciales. La cuenta de GitHub requiere autenticación propia.

## 3. Railway — Crear PostgreSQL

1. Abre `https://railway.com/` y crea un proyecto.
2. Añade un servicio **Postgres**. Espera a que su estado sea correcto.
3. Abre **Postgres → Variables** y comprueba disponibilidad de `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD`, `PGDATABASE`. No copies sus valores al repositorio.
4. La base de Railway estará inicialmente vacía. La aplicación la poblará con datos de demostración mediante JPA/seed si las variables se configuran bien.

## 4. Railway — Publicar Spring Boot

1. En el mismo proyecto crea un servicio desde el repositorio GitHub `novastore-cloud`.
2. En **Settings → Root Directory** escribe `/backend` (no `/`).
3. Build: **Dockerfile**; se incluye `backend/Dockerfile`. Railway puede detectar el archivo si la raíz está bien configurada.
4. En **Variables** del servicio API agrega las siguientes claves. Inserta referencias a las variables del servicio `Postgres` mediante el selector de Railway:

| Variable de API | Valor o referencia de Railway |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
| `SPRING_DATASOURCE_USERNAME` | `${{Postgres.PGUSER}}` |
| `SPRING_DATASOURCE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
| `ADMIN_EMAIL` | correo exclusivo del administrador |
| `ADMIN_USERNAME` | `admin` |
| `ADMIN_PASSWORD` | contraseña fuerte creada por ti, solo en Railway |
| `AUTH_REQUIRE_EMAIL_VERIFICATION` | `false` para práctica sin SMTP (ver nota) |
| `EMAIL_PROVIDER` | `disabled` durante práctica |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` provisional; añadir Vercel tras desplegar |

El servidor lee automáticamente `PORT` y escucha en `0.0.0.0`. No fuerces puerto `8091` ni `server.address=127.0.0.1`.

5. Realiza deploy y revisa logs; deben mostrar Spring Boot iniciado y **PostgreSQL conectado**.
6. En **Networking → Generate Domain** crea la URL pública de Railway (formato `https://...up.railway.app`).
7. Abre `https://TU_API.up.railway.app/api/health` y comprueba que devuelve HTTP `200` con `status=UP` y `database=UP`.

**Importante:** no publiques PostgreSQL con TCP Proxy para esta actividad. La API se conecta por la red privada de Railway.

## 5. Vercel — Publicar Angular

1. En `https://vercel.com/new` selecciona el repositorio GitHub `novastore-cloud`.
2. Configura **Root Directory: `frontend`**.
3. Build Command: `npm run build:vercel`.
4. Output Directory: `dist/novastore-angular/browser`.
5. Node.js: selecciona una versión **22.x compatible** si el panel lo permite.
6. Variable de entorno (Production, y Preview si corresponde):

```
API_BASE_URL=https://TU_API_REAL.up.railway.app
```

**Sin barra final y sin `/api`**, porque Angular añade el prefijo `/api/...`. El valor es una dirección pública, no una contraseña.

7. Pulsa Deploy. La compilación genera `src/app/core/config/api-url.ts` a partir de esa variable; no es necesario modificar cada servicio HTTP.
8. Copia la URL asignada por Vercel: `https://TU_PROYECTO.vercel.app`.
9. Abre una ruta profunda, por ejemplo `/acceso` o `/productos`, directamente en nueva pestaña. `vercel.json` mantiene la navegación Angular sin 404.

## 6. Railway — Ajustar CORS (obligatorio)

Una vez que sepas la URL exacta de Vercel, en el servicio API de Railway cambia:

```
CORS_ALLOWED_ORIGINS=https://TU_PROYECTO.vercel.app,http://localhost:4200
```

Usa la URL real **sin barra final**. Si utilizas un dominio personalizado o preview con otra URL, añádela separada por coma. No utilices `*` para resolver fallos de CORS.

Guarda y aplica el redeploy de Railway. Verifica el sitio nuevamente. Las peticiones `OPTIONS` con origen de Vercel deben recibir los encabezados CORS esperados.

## 7. Funcionalidad y permisos

- **Cualquier visitante** abre la URL pública en PC, Safari o Android.
- **USER** puede crear cuenta, iniciar sesión, ver catálogo y registrar un pedido de prueba (no paga).
- **ADMIN** puede iniciar sesión desde Vercel y administrar productos y clientes mediante `POST`, `GET`, `PUT`, `DELETE` (sin VPN).
- Spring Security controla el rol en el backend. Un `USER` no obtiene acceso por cambiar la URL en el navegador.
- Registros y productos se almacenan en PostgreSQL de Railway.

**Sobre el correo real:** `AUTH_REQUIRE_EMAIL_VERIFICATION=false` crea cuentas sin envío de código para evitar el bloqueo SMTP diagnosticado anteriormente. El correo escrito por el usuario no queda comprobado. Para enviar códigos reales, configura un proveedor de API por HTTPS, como Resend, con un dominio remitente verificado y estas variables en Railway:

```
AUTH_REQUIRE_EMAIL_VERIFICATION=true
EMAIL_PROVIDER=resend
RESEND_API_KEY=TU_CLAVE_PRIVADA
MAIL_FROM=NovaStore <verificacion@TU_DOMINIO_VERIFICADO.com>
```

No subas ninguna clave `RESEND_API_KEY` a GitHub ni a variables de Vercel; **solo al servicio API de Railway**. Sin credenciales válidas y dominio verificado, no habilites la verificación.

## 8. Pruebas y evidencias necesarias

1. Captura de GitHub mostrando `frontend`, `backend`, `database`, `docs`.
2. Captura de Vercel con estado Ready y URL pública real.
3. Captura de Railway API con estado Healthy/Active y dominio real.
4. Captura de Railway PostgreSQL conectado (oculta contraseñas).
5. Navegador en URL de Vercel, registro de cliente, login y catálogo.
6. Panel ADMIN: antes y después de crear, actualizar y eliminar producto de prueba.
7. PostgreSQL: confirma que la modificación persistió, mediante vista de datos o consulta autorizada.
8. DevTools → Network: solicitudes a URL de Railway con respuestas `200/201/204`.
9. Captura desde un segundo dispositivo conectado a Internet distinto.
10. Evidencia de CORS correcto y HTTPS sin exposición de claves.

Consulta `docs/PRUEBAS_FUNCIONALES.md` para la matriz.

## 9. Entrega al docente

Completa `docs/ENTREGA_URLS.md` con las URL **reales** de Vercel, Railway y GitHub. Completa el Word `docs/Informe_Tecnico_NovaStore_Cloud.docx` con tus capturas y resultados verificados.

## Referencias oficiales

- Vercel: https://vercel.com/docs/builds/configure-a-build
- SPA y rewrites: https://vercel.com/kb/guide/why-is-my-deployed-project-giving-404
- Railway monorepo: https://docs.railway.com/deployments/monorepo
- Railway healthcheck/PORT: https://docs.railway.com/deployments/healthchecks
- Railway PostgreSQL: https://docs.railway.com/guides/postgresql
- Railway pricing/free trial: https://docs.railway.com/pricing/free-trial
- Resend API: https://resend.com/docs/api-reference/emails/send-email
