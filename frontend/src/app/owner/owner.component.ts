import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ApiService, TransactionLog } from '../api.service';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.css']
})
export class OwnerComponent implements OnInit {
  allTransactions: TransactionLog[] = [];
  loading = true;
  error = '';

  constructor(
    private api: ApiService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
      return;
    }
    // For admin view, we could fetch transactions for all accounts
    // For now, we'll show a message that this feature requires admin backend
    this.loading = false;
    this.error = 'Owner dashboard requires admin endpoint implementation on backend.';
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
