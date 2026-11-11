# NovaStore Cloud — Gmail API OAuth 2.0 (sin SMTP)

Esta actualización conserva Angular en Vercel, Spring Boot en Railway, PostgreSQL y tus datos. **No reemplaza el proyecto completo**: modifica únicamente `MailService.java`, `application.properties` y `.gitignore`, y agrega `tools/autorizar-gmail.mjs`.

## A. Autorizar Gmail en TU computadora

1. Confirma que el JSON descargado de Google Cloud corresponde a **Aplicación de escritorio** y que el correo remitente está en `Google Auth Platform → Público → Usuarios de prueba`. El permiso `gmail.send` debe estar en Acceso a los datos.
2. Cierra el JSON y guárdalo **fuera de la carpeta de GitHub**, por ejemplo, en Descargas. No lo subas, ni lo adjuntes a un chat.
3. Copia el contenido del ZIP sobre la raíz de tu copia de `novastore-cloud` en Visual Studio Code, combinando carpetas y reemplazando solo los archivos coincidentes.
4. En una terminal de VS Code ubicada en la raíz del proyecto, ejecuta (sustituye nombres y dirección):

   ```powershell
   node .\tools\autorizar-gmail.mjs "C:\Users\santa\Downloads\client_secret_TU_ARCHIVO.json" "tu-remitente@gmail.com"
   ```

5. El comando imprimirá una URL de autorización de Google. Abre esa URL en el **navegador de la misma PC**. Selecciona el Gmail remitente correcto, acepta únicamente el permiso de enviar correos. La ventana que Google abre de vuelta en `127.0.0.1` terminará el flujo.
6. El comando **no muestra tus claves**. Guardará la configuración privada en `%USERPROFILE%\.novastore-private\railway-gmail.env`. Mantén ese archivo privado; no lo copies al proyecto ni lo subas a GitHub.

**Si recibes `access_denied`**, verifica que la cuenta autorizada sea usuario de prueba en el proyecto correcto. **Si no llega el refresh token**, revoca el permiso anterior desde la cuenta Google y vuelve a ejecutar. Los tokens OAuth de proyectos externos en estado *Prueba* normalmente caducan a los siete días.

## B. Publicar el código Java, SIN activar aún correo real

Desde la carpeta raíz del repositorio en VS Code:

```powershell
git add .gitignore backend/src/main/java/com/novastore/api/service/MailService.java backend/src/main/resources/application.properties tools/autorizar-gmail.mjs GUIA_GMAIL_API.md
git commit -m "Integrar Gmail API OAuth2 para verificacion de NovaStore"
git push origin main
```

GitHub debe compilar el backend y Railway debe quedar `ACTIVE` con `/api/health` operativo. Mientras tanto **deja** `AUTH_REQUIRE_EMAIL_VERIFICATION=false` y `EMAIL_PROVIDER=disabled` en Railway.

## C. Configurar Gmail en Railway (cuando el backend nuevo ya esté activo)

En Railway → servicio `novastore-cloud` → **Variables**, añade los datos del archivo privado `%USERPROFILE%\.novastore-private\railway-gmail.env`:

| Variable | Valor |
|---|---|
| `GMAIL_CLIENT_ID` | ID OAuth de tu JSON / archivo privado |
| `GMAIL_CLIENT_SECRET` | secreto OAuth privado |
| `GMAIL_REFRESH_TOKEN` | refresh token del script |
| `MAIL_FROM` | Gmail que autorizaste |
| `EMAIL_PROVIDER` | `gmail-api` |
| `AUTH_REQUIRE_EMAIL_VERIFICATION` | `true` |

Aplica Deploy. **No configures estas variables en Vercel**; el correo sale desde Spring Boot. Nunca las escribas en `application.properties`.

## D. Prueba de funcionamiento

1. Comprueba que `https://novastore-cloud-production.up.railway.app/api/health` indique `UP`.
2. Desde `https://novastore-cloud.vercel.app`, crea una cuenta **nueva**, con un correo que no exista.
3. Recibe el código de seis dígitos, introdúcelo en **Ya tengo un código**, y luego inicia sesión.
4. Prueba correo en otra plataforma (por ejemplo Outlook). Las cuentas anteriormente creadas sin verificación siguen marcadas como verificadas.

**Si falla**: consulta `Deploy Logs` de Railway. HTTP 400 de OAuth frecuentemente significa refresh token vencido/revocado; HTTP 403 de Gmail API puede indicar falta de permiso, cuenta equivocada o política Google; HTTP 429 significa cuotas. No copies tokens ni respuestas con credenciales.

## E. Restricciones y seguridad

- Google OAuth con estado **Prueba** emite refresh tokens con vida habitual de **7 días** al solicitar `gmail.send`. Esta solución es útil para aprender y demostrar el envío, **no es una solución de correo de producción estable** sin resolver la publicación/verificación OAuth.
- Tu Gmail personal tiene límites y políticas antispam. Antes de abrir registros masivos agrega límites de envío por IP y por correo, detección de abuso, registro seguro de errores y un proveedor de correo transaccional con dominio verificado.
- No alteres credenciales de PostgreSQL ni la API URL de Vercel. El registro público no crea cuentas `ADMIN`; se protege como antes.
- Mantén revocada cualquier contraseña antigua de aplicación Gmail compartida en conversaciones previas. Esta integración no usa contraseñas de aplicación.

**Validación del parche**: sintaxis Java (javac 17 con clases de integración simuladas), sintaxis Node (`node --check`) e integridad ZIP. **No se ha probado un envío real sin autorización de tu Gmail ni ejecutado Maven completo aquí.**
