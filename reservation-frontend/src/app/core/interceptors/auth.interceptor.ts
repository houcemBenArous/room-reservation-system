import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const token = authService.getToken();

    // DEBUG: Log token status
    console.log('[Interceptor] Request URL:', req.url);
    console.log('[Interceptor] Token exists:', !!token);
    if (token) {
        console.log('[Interceptor] Token (first 50 chars):', token.substring(0, 50) + '...');
    }

    if (token) {
        const clonedReq = req.clone({
            setHeaders: {
                Authorization: `Bearer ${token}`
            }
        });
        console.log('[Interceptor] Authorization header added');
        return next(clonedReq);
    }

    console.log('[Interceptor] No token, request sent without Authorization header');
    return next(req);
};
