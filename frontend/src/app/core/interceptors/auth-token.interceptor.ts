import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthService } from '../services/auth.service';

/** Envía tokens exclusivamente a rutas API del propio proyecto. */
export const authTokenInterceptor: HttpInterceptorFn = (request, next) => {
  // No adjuntar tokens a recursos externos ni a las operaciones públicas de acceso.
  if (!request.url.startsWith('/api/') ||
      /^\/api\/auth\/(?:login|register|verify|resend)(?:[/?]|$)/.test(request.url)) {
    return next(request);
  }

  const session = inject(AuthService).session();
  if (!session?.token) return next(request);
  return next(request.clone({ setHeaders: { Authorization: `Bearer ${session.token}` } }));
};
