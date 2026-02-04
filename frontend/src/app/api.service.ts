import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Account {
  id: number;
  username: string;
  holderName: string;
  balance: number;
  status: string;
  lastUpdated: string;
}

export interface BalanceResponse {
  accountId: number;
  balance: number;
}

export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  idempotencyKey: string;
}

export interface TransferResponse {
  transactionId: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: string;
  message: string;
  timestamp: string;
}

export interface TransactionLog {
  id: string;
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  status: string;
  failureReason: string;
  idempotencyKey: string;
  createdOn: string;
}

// New interface for transaction history response
export interface TransactionHistoryResponse {
  transactionId: string;
  fromAccountId: number;
  fromAccountUsername: string;
  toAccountId: number;
  toAccountUsername: string;
  amount: number;
  status: string;
  timestamp: string;
}

export interface LoginResponse {
  accountId: number;
  username: string;
  holderName: string;
  balance: number;
  status: string;
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private baseUrl = 'http://localhost:8080/api/v1';

  constructor(private http: HttpClient) { }

  // Auth endpoint
  login(username: string): Observable<LoginResponse> {
    return this.http.get<LoginResponse>(`${this.baseUrl}/auth/login?username=${encodeURIComponent(username)}`);
  }

  // Account endpoints
  getAccount(accountId: number): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/accounts/${accountId}`);
  }

  getAccountByUsername(username: string): Observable<Account> {
    return this.http.get<Account>(`${this.baseUrl}/accounts/by-username/${encodeURIComponent(username)}`);
  }

  getBalance(accountId: number): Observable<BalanceResponse> {
    return this.http.get<BalanceResponse>(`${this.baseUrl}/accounts/${accountId}/balance`);
  }

  // Old method for backward compatibility
  getTransactions(accountId: number): Observable<TransactionLog[]> {
    return this.http.get<TransactionLog[]>(`${this.baseUrl}/accounts/${accountId}/transactions`);
  }

  /**
   * Get transaction history based on user role.
   * - ADMIN: Returns all transactions
   * - USER: Returns only their transactions
   */
  getTransactionHistory(): Observable<TransactionHistoryResponse[]> {
    return this.http.get<TransactionHistoryResponse[]>(`${this.baseUrl}/transfers`);
  }

  /**
   * Get all transactions (ADMIN only).
   */
  getAllTransactions(): Observable<TransactionHistoryResponse[]> {
    return this.http.get<TransactionHistoryResponse[]>(`${this.baseUrl}/transfers/all`);
  }

  // Transfer endpoint
  transfer(request: TransferRequest): Observable<TransferResponse> {
    return this.http.post<TransferResponse>(`${this.baseUrl}/transfers`, request);
  }

  // Helper to generate idempotency key
  generateIdempotencyKey(): string {
    return 'txn-' + Date.now() + '-' + Math.random().toString(36).substring(2, 9);
  }
}
