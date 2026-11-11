import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class ApiStatusService {
  private readonly http = inject(HttpClient);
  readonly online = signal<boolean | null>(null);
  readonly message = signal('Comprobando API…');

  check(): void {
    this.http.get<{ status: string; service: string; database: string }>('/api/health').subscribe({
      next: data => {
        this.online.set(data.status === 'UP' && data.database === 'UP');
        this.message.set(data.database === 'UP' ? `${data.service} y PostgreSQL conectados` : 'Base de datos no disponible');
      },
      error: () => {
        this.online.set(false);
        this.message.set('API REST desconectada');
      }
    });
  }
}
