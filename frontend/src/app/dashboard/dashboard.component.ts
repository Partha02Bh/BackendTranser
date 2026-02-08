import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  user: any;
  account: any;
  history: any[] = [];
  targetId: any;
  amount: any;

  showSuccessModal = false;
  lastTransaction = { targetId: 0, amount: 0 };

  isAdmin = false;

  constructor(private api: ApiService, private router: Router) { }

  ngOnInit() {
    this.isAdmin = this.api.isAdmin();
    const userData = localStorage.getItem('user');
    if (!userData) {
      this.router.navigate(['/']);
      return;
    }
    this.user = JSON.parse(userData);
    this.loadData();
  }

  loadData() {
    this.api.getAccount(this.user.accountId).subscribe(data => {
      console.log('Account Data:', data);  // Debugging log
      this.account = data;
      this.api.getHistory(this.account.id).subscribe(hist => {
        console.log('Transaction History:', hist);  // Debugging log
        this.history = hist;
      });
    });
  }

  sendMoney() {
    if (!this.targetId || !this.amount) return;

    this.api.transfer(this.account.id, Number(this.targetId), Number(this.amount)).subscribe({
      next: () => {
        // Capture transaction details for the modal
        this.lastTransaction = {
          targetId: this.targetId,
          amount: this.amount
        };

        this.loadData(); // Refresh balance and history
        this.showSuccessModal = true; // Show success modal

        // Clear inputs
        this.targetId = '';
        this.amount = '';
      },
      error: () => alert("Transfer Failed! Check balance or ID.")
    });
  }

  closeModal() {
    this.showSuccessModal = false;
  }

  logout() {
    localStorage.clear();
    this.router.navigate(['/']);
  }
}
