# Modelo de negocio escalable — NovaStore

**Propuesta:** tienda digital especializada en accesorios tecnológicos para estudiantes, jugadores y profesionales. Proyecto académico, no tienda comercial operativa.

**Segmentos:** estudiantes, aficionados al gaming, trabajadores remotos y creadores de contenido.

**Valor:** catálogo curado, búsqueda, fichas de producto, carrito y registro de pedidos de demostración.

**Canales:** web responsive pública en Vercel, atención digital futura, campañas de contenido.

**Ingresos futuros:** margen por venta, combos y servicios de instalación (no incluidos en la entrega actual).

**Costos futuros:** inventario real, pagos, envíos, impuestos, nube y atención a clientes. La demo no procesa pagos reales.

**Arquitectura evolutiva:** monolito modular Spring Boot y Angular separados, API REST, PostgreSQL, permisos por rol. Fases: 1) catálogo y CRUD; 2) control de stock/auditoría; 3) pasarela de pagos y logística; 4) observabilidad, migraciones versionadas, backups, control antifraude y optimización.

**KPIs sugeridos:** productos activos, conversiones de carrito de prueba, tasa de errores, latencia API, tasa de registros y consistencia de stock.

**Riesgos y mitigaciones:** saturación por plan gratuito → monitoreo/costos; correo sin verificar en demo → activar Resend HTTPS; credenciales → variables seguras; roles → validación backend; hibernate auto-DDL → migraciones cuando evolucione a producción.
