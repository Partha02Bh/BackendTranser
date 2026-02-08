import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';


@Component({
    selector: 'app-history',
    templateUrl: './history.component.html',
    styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
    user: any;
    account: any;
    history: any[] = [];
    isAdmin = false;

    constructor(private api: ApiService, private router: Router) { }

    ngOnInit(): void {
        this.user = this.api.getCurrentUser();
        this.isAdmin = this.api.isAdmin();
        if (this.user && this.user.accountId) {
            this.loadData();
        }
    }

    logout() {
        localStorage.clear();
        this.router.navigate(['/']);
    }

    loadData() {
        this.api.getAccount(this.user.accountId).subscribe(data => {
            this.account = data;
            this.api.getHistory(this.account.id).subscribe(hist => {
                this.history = hist;
            });
        });
    }
}
