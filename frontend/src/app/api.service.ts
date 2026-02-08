import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginResponse {
  accountId: number;
  username: string;
  holderName: string;
  balance: number;
  status: string;
  message: string;
  token: string;
  role?: string;
}

export interface OtpResponse {
  message: string;
  username: string;
  otp: string;  // For demo only - remove in production
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) { }

  /**
   * Step 1: Login with credentials - returns OTP
   */
  login(username: string, password: string, role: string): Observable<OtpResponse> {
    return this.http.post<OtpResponse>(`${this.baseUrl}/auth/login`, { username, password, role });
  }

  /**
   * Step 2: Verify OTP - returns JWT token
   */
  verifyOtp(username: string, otp: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.baseUrl}/auth/verify-otp`, { username, otp })
      .pipe(
        tap(response => {
          if (response.token) {
            localStorage.setItem('jwt_token', response.token);
            localStorage.setItem('user', JSON.stringify({
              accountId: response.accountId,
              username: response.username,
              holderName: response.holderName,
              balance: response.balance,
              status: response.status,
              role: response.role // Store role
            }));
          }
        })
      );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.baseUrl}/auth/register`, userData);
  }

  logout(): void {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user');
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getCurrentUser(): any {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  isAdmin(): boolean {
    const user = this.getCurrentUser();
    return user && (user.role === 'ADMIN' || user.role === 'OWNER');
  }

  getAccount(accountId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/accounts/${accountId}`);
  }

  getTransactionHistory(accountId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/accounts/${accountId}/transactions`);
  }

  // Alias for backward compatibility
  getHistory(accountId: number): Observable<any[]> {
    return this.getTransactionHistory(accountId);
  }

  getAllTransactions(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/transfers/all`);
  }

  transfer(sourceId: number, targetId: number, amount: number): Observable<any> {
    const idempotencyKey = this.generateIdempotencyKey();
    return this.http.post<any>(`${this.baseUrl}/transfers`, {
      fromAccountId: sourceId,
      toAccountId: targetId,
      amount: amount,
      idempotencyKey: idempotencyKey
    });
  }

  private generateIdempotencyKey(): string {
    return `${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
  }
}
