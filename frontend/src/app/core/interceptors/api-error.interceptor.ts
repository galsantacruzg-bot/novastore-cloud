import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/** Gestiona sesiones caducadas sin confundir errores de credenciales con caídas de la API. */
export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return next(req).pipe(catchError((error: HttpErrorResponse) => {
    // Solo invalidar si la respuesta 401 corresponde exactamente al token usado
    // en la petición. Evita borrar una nueva sesión por una respuesta retrasada.
    const authorization = req.headers.get('Authorization') ?? '';
    const usedToken = authorization.startsWith('Bearer ')
      ? authorization.slice('Bearer '.length)
      : null;
    const activeSession = auth.session();

    if (error.status === 401 && req.url.startsWith('/api/') && usedToken &&
        activeSession?.token === usedToken) {
      const target = '/acceso';
      auth.clearLocalSession();
      void router.navigateByUrl(target);
    }

    const serverMessage = typeof error.error?.message === 'string' ? error.error.message : '';
    const userMessage = error.status === 0
      ? 'No hay conexión con NovaStore. Comprueba el backend y tu red.'
      : error.status === 401 ? 'La sesión expiró o las credenciales no son válidas. Inicia sesión de nuevo.'
      : error.status === 403 ? (serverMessage || 'Solicitud rechazada (403). Si ocurre al registrarte desde HTTPS, revisa la configuración CORS y reinicia el backend actualizado.')
      : error.status === 409 ? (serverMessage || 'Conflicto de datos o stock insuficiente.')
      : error.status === 503 ? (serverMessage || 'Servicio temporalmente no disponible. Consulta los registros de Railway.')
      : serverMessage || (error.status >= 500 ? 'Error interno del servidor.' : 'No se pudo completar la solicitud.');

    return throwError(() => Object.assign(error, { userMessage }));
  }));
};
