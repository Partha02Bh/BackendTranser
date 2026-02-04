import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * HTTP Interceptor to attach Authorization header to all API requests.
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private authService: AuthService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const authHeader = this.authService.getAuthHeader();

        if (authHeader && request.url.includes('/api/')) {
            const authReq = request.clone({
                setHeaders: {
                    Authorization: authHeader
                }
            });
            return next.handle(authReq);
        }

        return next.handle(request);
    }
}
