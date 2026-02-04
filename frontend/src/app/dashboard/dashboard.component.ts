import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService, Account, TransactionHistoryResponse, TransferRequest } from '../api.service';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  account: Account | null = null;
  transactions: TransactionHistoryResponse[] = [];

  // Transfer form
  targetAccountId: number | null = null;
  transferAmount: number | null = null;
  transferLoading = false;
  transferError = '';
  transferSuccess = '';

  // Role-based properties
  isAdmin = false;
  canTransfer = false;

  constructor(
    private apiService: ApiService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
      return;
    }

    // Set role-based flags
    this.isAdmin = this.authService.isAdmin();
    this.canTransfer = this.authService.canTransfer();

    this.loadData();
  }

  loadData() {
    const user = this.authService.getCurrentUser();
    if (!user) return;

    // For admin, we don't need to load account details
    if (!this.isAdmin && user.accountId) {
      this.apiService.getAccount(user.accountId).subscribe({
        next: (account) => {
          this.account = account;
        },
        error: (err) => {
          console.error('Error loading account:', err);
        }
      });
    }

    // Load transactions based on role (API handles role-based filtering)
    this.loadTransactions();
  }

  loadTransactions() {
    this.apiService.getTransactionHistory().subscribe({
      next: (transactions) => {
        this.transactions = transactions.sort((a, b) =>
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        );
      },
      error: (err) => {
        console.error('Error loading transactions:', err);
      }
    });
  }

  sendMoney() {
    if (!this.canTransfer) {
      this.transferError = 'You are not authorized to make transfers.';
      return;
    }

    if (!this.targetAccountId || !this.transferAmount || !this.account) return;

    this.transferLoading = true;
    this.transferError = '';
    this.transferSuccess = '';

    const request: TransferRequest = {
      fromAccountId: this.account.id,
      toAccountId: this.targetAccountId,
      amount: this.transferAmount,
      idempotencyKey: this.apiService.generateIdempotencyKey()
    };

    this.apiService.transfer(request).subscribe({
      next: (response) => {
        this.transferLoading = false;
        this.transferSuccess = `Transfer successful! Transaction ID: ${response.transactionId}`;
        this.targetAccountId = null;
        this.transferAmount = null;
        this.loadData();
      },
      error: (err) => {
        this.transferLoading = false;
        if (err.status === 403) {
          this.transferError = 'You are not authorized to make transfers.';
        } else {
          this.transferError = err.error?.message || 'Transfer failed. Please check balance and account ID.';
        }
      }
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }

  getTransactionType(txn: TransactionHistoryResponse): string {
    if (!this.account) return '';
    return txn.fromAccountId === this.account.id ? 'SENT' : 'RECEIVED';
  }

  getTransactionAmount(txn: TransactionHistoryResponse): string {
    if (!this.account) {
      // For admin view, just show the amount
      return `$${txn.amount.toFixed(2)}`;
    }
    const prefix = txn.fromAccountId === this.account.id ? '-' : '+';
    return `${prefix}$${txn.amount.toFixed(2)}`;
  }

  getCurrentUsername(): string {
    return this.authService.getCurrentUser()?.username || '';
  }
}
