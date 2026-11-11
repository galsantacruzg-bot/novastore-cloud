# Resolución de problemas de despliegue

| Síntoma | Diagnóstico principal | Acción concreta |
|---|---|---|
| Railway 502 | Backend no escucha en host/puerto | Verificar `PORT` y `server.address=0.0.0.0`, Railway Root `/backend` |
| Railway healthcheck fallido | API o PostgreSQL no inicia | Ver logs; referencias `${{Postgres.PGHOST}}`, usuario, contraseña y DB |
| Maven BUILD FAILURE | Dependencia o compilación | Revisar primeras líneas `Caused by` en Railway build logs; CI GitHub Actions |
| Angular publica pero no aparecen productos | `API_BASE_URL` no configurado en Vercel o error API | Configurar URL HTTPS Railway; redeploy Vercel; inspeccionar Network |
| CORS 403 | Dominio Vercel no autorizado | Añadir URL EXACTA a `CORS_ALLOWED_ORIGINS` en Railway y redeploy |
| CORS en preflight | OPTIONS bloqueado | Revisa `SecurityConfig` y `WebConfig`; no quitar seguridad por completo |
| Vercel 404 al refrescar `/gestion` | Routing SPA sin fallback | `frontend/vercel.json` con rewrite hacia `index.html` |
| Registro se queda en código | `AUTH_REQUIRE_EMAIL_VERIFICATION=true` sin proveedor real | Para demo usar `false`; para producción configurar Resend HTTPS y dominio verificado |
| SMTP 587/465 bloqueado | Red o proveedor bloquea salida SMTP | La versión Cloud no depende de SMTP; usa API HTTPS opcional |
| ADMIN no ingresa | `ADMIN_EMAIL` ya pertenece a USER o clave vieja | Corregir en Railway variables, revisar logs; no elevar USER automáticamente |
| CRUD devuelve 403 | Usuario no tiene rol ADMIN | Inicio de sesión con cuenta ADMIN creada desde variables del backend |
| PostgreSQL parece vacío | API apunta a BD local u otra instancia | Comprobar referencias correctas de Railway; evitar mezclar DB local y nube |
| Railway Free agotado | Crédito/recursos insuficientes | Revisar uso, aumentar recursos o pausar servicios; no se garantiza hosting gratis ilimitado |

No se debe solucionar un error quitando autenticación, poniendo `CORS_ALLOWED_ORIGINS=*`, publicando PostgreSQL, o subiendo contraseñas al repositorio.
