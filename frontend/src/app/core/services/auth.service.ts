import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthSession, RegisterRequest } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly storageKey = 'novastore_session';
  readonly session = signal<AuthSession | null>(this.readSession());
  readonly isLoggedIn = computed(() => !!this.session());
  readonly isAdmin = computed(() => this.session()?.role === 'ADMIN');

  register(request: RegisterRequest): Observable<{message: string; verificationRequired: boolean}> {
    return this.http.post<{message: string; verificationRequired: boolean}>('/api/auth/register', request);
  }

  verify(email: string, code: string): Observable<{message: string}> {
    return this.http.post<{message: string}>('/api/auth/verify', { email, code });
  }

  resend(email: string): Observable<{message: string}> {
    return this.http.post<{message: string}>('/api/auth/resend', { email });
  }

  login(identifier: string, password: string): Observable<AuthSession> {
    return this.http.post<AuthSession>('/api/auth/login', { identifier, password }).pipe(
      tap(session => this.saveSession(session))
    );
  }

  logout(): void {
    // Enviar la petición antes de retirar el token; el interceptor la autenticará.
    this.http.post('/api/auth/logout', {}).subscribe({ error: () => void 0 });
    this.clearLocalSession();
  }

  /** Limpia solo la sesión local, sin intentar un logout con un token ya vencido. */
  clearLocalSession(): void {
    try {
      localStorage.removeItem(this.storageKey);
    } catch {
      // El estado en memoria debe limpiarse incluso si el navegador bloquea el almacenamiento.
    }
    this.session.set(null);
  }

  private readSession(): AuthSession | null {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (!raw) return null;
      const candidate: unknown = JSON.parse(raw);
      if (!this.isValidSession(candidate)) {
        localStorage.removeItem(this.storageKey);
        return null;
      }
      return candidate;
    } catch {
      try { localStorage.removeItem(this.storageKey); } catch { /* almacenamiento no disponible */ }
      return null;
    }
  }

  private isValidSession(value: unknown): value is AuthSession {
    if (typeof value !== 'object' || value === null) return false;
    const session = value as Partial<AuthSession>;
    return typeof session.token === 'string' && session.token.length > 0
      && typeof session.userId === 'number'
      && typeof session.username === 'string'
      && typeof session.email === 'string'
      && (session.role === 'ADMIN' || session.role === 'USER');
  }

  private saveSession(session: AuthSession): void {
    this.session.set(session);
    try {
      localStorage.setItem(this.storageKey, JSON.stringify(session));
    } catch {
      // La sesión sigue operativa en memoria hasta actualizar la página.
    }
  }
}
