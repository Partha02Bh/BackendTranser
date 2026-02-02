import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';
@Component({
  selector: 'app-owner',
  templateUrl: './owner.component.html',
  styleUrls: ['./owner.component.css']
})
export class OwnerComponent implements OnInit {
  allTransactions: any[] = [];

  constructor(private api: ApiService, private router: Router){}

  ngOnInit() {
    this.api.getAllTransactions().subscribe(data => {
      this.allTransactions = data
    });
  }
  logout(){
    this.router.navigate(['/']);
  }
}
