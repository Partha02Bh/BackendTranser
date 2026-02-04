import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';
import { ApiService } from '../api.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private apiService: ApiService,
    private router: Router
  ) { }

  onLogin() {
    this.error = '';
    this.loading = true;

    // First set credentials for Basic Auth header
    this.authService.login(this.username, this.password, 0, '');

    // Call login API to get account details for the username
    this.apiService.login(this.username).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.accountId) {
          // Update auth service with full account details
          this.authService.login(this.username, this.password, response.accountId, response.holderName);
          // Store account info
          localStorage.setItem('account', JSON.stringify({
            id: response.accountId,
            holderName: response.holderName,
            balance: response.balance,
            status: response.status
          }));
          this.router.navigate(['/dashboard']);
        } else {
          this.authService.logout();
          this.error = response.message || 'Login failed. Please try again.';
        }
      },
      error: (err) => {
        this.loading = false;
        this.authService.logout();
        this.error = 'Invalid credentials. Please try again.';
      }
    });
  }
}
