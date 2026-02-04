import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface User {
    username: string;
    accountId: number;
    holderName: string;
    role: 'ADMIN' | 'USER';
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private credentials: { username: string; password: string } | null = null;
    private currentUserSubject = new BehaviorSubject<User | null>(null);
    currentUser$ = this.currentUserSubject.asObservable();

    constructor() {
        // Check for stored credentials
        const stored = sessionStorage.getItem('mts_user');
        if (stored) {
            this.currentUserSubject.next(JSON.parse(stored));
            const creds = sessionStorage.getItem('mts_creds');
            if (creds) {
                this.credentials = JSON.parse(creds);
            }
        }
    }

    /**
     * Login with Basic Auth credentials.
     * Sets the user object with account details and role.
     */
    login(username: string, password: string, accountId: number, holderName: string): boolean {
        this.credentials = { username, password };

        // Determine role based on username
        const role: 'ADMIN' | 'USER' = username.toLowerCase() === 'admin' ? 'ADMIN' : 'USER';

        const user: User = { username, accountId, holderName, role };

        sessionStorage.setItem('mts_creds', JSON.stringify(this.credentials));
        sessionStorage.setItem('mts_user', JSON.stringify(user));
        this.currentUserSubject.next(user);

        return true;
    }

    /**
     * Logout and clear credentials.
     */
    logout(): void {
        this.credentials = null;
        sessionStorage.removeItem('mts_creds');
        sessionStorage.removeItem('mts_user');
        this.currentUserSubject.next(null);
    }

    /**
     * Get Basic Auth header value.
     */
    getAuthHeader(): string | null {
        if (!this.credentials) {
            const creds = sessionStorage.getItem('mts_creds');
            if (creds) {
                this.credentials = JSON.parse(creds);
            }
        }

        if (this.credentials) {
            const token = btoa(`${this.credentials.username}:${this.credentials.password}`);
            return `Basic ${token}`;
        }
        return null;
    }

    /**
     * Check if user is logged in.
     */
    isLoggedIn(): boolean {
        return this.currentUserSubject.value !== null;
    }

    /**
     * Get current user.
     */
    getCurrentUser(): User | null {
        return this.currentUserSubject.value;
    }

    /**
     * Check if current user is admin.
     */
    isAdmin(): boolean {
        return this.currentUserSubject.value?.role === 'ADMIN';
    }

    /**
     * Check if current user can transfer money.
     */
    canTransfer(): boolean {
        return this.currentUserSubject.value?.role === 'USER';
    }
}
