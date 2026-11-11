# Arquitectura técnica

```text
                         NAVEGADORES WEB
                     HTTPS (iPhone / PC / Android)
                                 |
                 VERCEL · ANGULAR (SPA publica)
                                 |
                   API_BASE_URL (durante build)
                                 |
                  HTTPS + CORS + Bearer Token
                                 |
                 RAILWAY · SPRING BOOT REST API
             /api/auth  /api/products  /api/customers
                              /api/orders
                                 |
                          JDBC privado
                                 |
                      RAILWAY · POSTGRESQL

     GitHub: monorepo frontend/ backend/ database/ docs/
     GitHub Actions: pruebas Java + compilación Angular
```

**Flujo del registro:** Angular envía `POST /api/auth/register` → API valida → JPA guarda cuenta `USER`. Para demo `AUTH_REQUIRE_EMAIL_VERIFICATION=false`: acceso inmediatamente habilitado; el correo no queda verificado realmente. Si se activa Resend HTTPS con dominio validado se envía código de seis dígitos y se requiere `POST /api/auth/verify`.

**Flujo CRUD:** ADMIN inicia sesión → obtiene token Bearer → peticiones `POST/PUT/DELETE` a Railway → Spring Security comprueba rol ADMIN → servicio aplica negocio → repositorio persiste en PostgreSQL → Angular actualiza vista.

**Separación de responsabilidades:** Vercel solo publica assets estáticos; Railway API decide permisos y transacciones; PostgreSQL queda privado. Cambiar frontend no concede permisos adicionales.
