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

  constructor(private api: ApiService, private router: Router){}

  ngOnInit(){
    const userData = localStorage.getItem('user');
    if(!userData){
      this.router.navigate(['/']);
      return;
    }
    this.user = JSON.parse(userData);
    this.loadData();
  }
  loadData(){
    this.api.getAccount(this.user.id).subscribe(data => {
      this.account = data;
      this.api.getHistory(this.account.id).subscribe(hist=>{
        this.history = hist;
     });
    });
  }
  sendMoney(){
    if(!this.targetId || !this.amount) return;

    this.api.transfer(this.account.id, Number(this.targetId), Number(this.amount)).subscribe({
      next : () => {
        alert("Transaction Successfull!");
        this.loadData(); // referesh after send the money
        this.targetId = '';
        this.amount = '';
      },
      error: () => alert("Transfer Failed! Check balance or ID.")
    });
  }
  logout(){
    localStorage.clear();
    this.router.navigate(['/']);
  }
}
