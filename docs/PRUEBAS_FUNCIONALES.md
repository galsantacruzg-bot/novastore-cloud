# Matriz de pruebas obligatorias (marcar solo al verificar)

| # | Prueba | Acción y resultado esperado | Evidencia real |
|---|---|---|---|
| P01 | API saludable | `GET /api/health` → 200; `database: UP` | [ ] |
| P02 | Página Vercel | Abrir URL desde navegador distinto a servidor → HTML, CSS, JS | [ ] |
| P03 | Angular–API | Network muestra tráfico al dominio Railway, sin `localhost` | [ ] |
| P04 | CORS | Preflight desde origen Vercel autorizado, sin `403` | [ ] |
| P05 | Registro | `POST /api/auth/register` → 201; en modo demo login disponible | [ ] |
| P06 | Inicio de sesión | `POST /api/auth/login` → 200; rol USER o ADMIN | [ ] |
| P07 | Crear producto | ADMIN: `POST /api/products` → 201 | [ ] |
| P08 | Consultar producto | ADMIN: `GET /api/products` → 200; producto presente | [ ] |
| P09 | Actualizar producto | ADMIN: `PUT /api/products/{id}` → 200; cambio persistente | [ ] |
| P10 | Eliminar producto | ADMIN: `DELETE /api/products/{id}` → 204; producto ya no está | [ ] |
| P11 | Clientes CRUD | ADMIN: crear, listar, actualizar, eliminar cliente de prueba | [ ] |
| P12 | Seguridad roles | USER no puede realizar acciones ADMIN → 403 | [ ] |
| P13 | Pedido demostrativo | Usuario autenticado confirma carrito y descuenta stock | [ ] |
| P14 | Persistencia | Reiniciar la API; los cambios permanecen en PostgreSQL | [ ] |
| P15 | Dispositivo externo | Abrir URL Vercel desde iPhone con datos móviles sin VPN | [ ] |

## Evidencias

Guarda capturas fechadas en `docs/evidencias/` (crear esta carpeta al preparar entrega). **No capturar contraseñas, claves API, tokens ni cadenas de conexión privadas.**

## Prueba opcional automatizada de salud y CORS

Desde la raíz del proyecto (necesitas Node.js):

```powershell
node scripts/comprobar-despliegue.mjs https://TU-API.up.railway.app https://TU-WEB.vercel.app
```

El script verifica salud, lectura API y una solicitud preflight CORS. No crea, modifica ni elimina datos.
