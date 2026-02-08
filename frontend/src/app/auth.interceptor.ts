import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private router: Router) { }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const token = localStorage.getItem('jwt_token');

        // Skip adding token for login requests
        if (request.url.includes('/auth/login')) {
            return next.handle(request);
        }

        // Add JWT token to request headers
        if (token) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }

        return next.handle(request).pipe(
            catchError((error: HttpErrorResponse) => {
                if (error.status === 401 || error.status === 403) {
                    // Token expired or invalid - redirect to login
                    localStorage.removeItem('jwt_token');
                    localStorage.removeItem('user');
                    this.router.navigate(['/login']);
                }
                return throwError(() => error);
            })
        );
    }
}
