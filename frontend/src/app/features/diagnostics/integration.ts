import { Component, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({ selector: 'app-integration', standalone: true, templateUrl: './integration.html', styleUrls: ['./integration.css'] })
export class IntegrationComponent {
  private readonly http = inject(HttpClient);
  readonly url = window.location.origin;
  readonly health = signal('Pendiente de comprobación');
  readonly database = signal('Sin comprobar');
  readonly checking = signal(false);
  readonly endpoints = [
    ['GET', '/api/health', 'Diagnóstico real Spring Boot y PostgreSQL'],
    ['GET', '/api/products', 'Catálogo autenticado desde la base'],
    ['POST', '/api/orders/guest', 'Pedido demo (sesión obligatoria y acceso HTTPS)'],
    ['POST', '/api/auth/register', 'Crear cuenta de cliente'],
    ['POST', '/api/auth/verify', 'Verificar código por correo'],
    ['POST', '/api/auth/login', 'Autenticación'],
    ['POST', '/api/products', 'ADMIN: crear producto'],
    ['PUT', '/api/products/{id}', 'ADMIN: editar producto'],
    ['DELETE', '/api/products/{id}', 'ADMIN: borrar producto'],
    ['GET', '/api/customers', 'ADMIN: consultar clientes'],
    ['GET', '/api/orders', 'ADMIN: consultar pedidos']
  ];
  check(): void {
    this.checking.set(true);
    this.http.get<{status: string; database: string}>('/api/health').subscribe({
      next: x => { this.health.set(x.status); this.database.set(x.database); this.checking.set(false); },
      error: () => { this.health.set('Sin conexión'); this.database.set('No disponible'); this.checking.set(false); }
    });
  }
}
