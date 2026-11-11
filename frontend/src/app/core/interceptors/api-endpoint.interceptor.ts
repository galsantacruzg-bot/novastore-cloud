import { HttpInterceptorFn } from '@angular/common/http';
import { API_BASE_URL } from '../config/api-url';

/** /api/... en desarrollo, https://servicio.up.railway.app/api/... en Vercel. */
export const apiEndpointInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/') || !API_BASE_URL) return next(request);
  return next(request.clone({ url: `${API_BASE_URL}${request.url}` }));
};
