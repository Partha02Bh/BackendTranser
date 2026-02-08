import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
    user: any;
    account: any;

    constructor(private api: ApiService, private router: Router) { }

    ngOnInit() {
        const userData = localStorage.getItem('user');
        if (!userData) {
            this.router.navigate(['/']);
            return;
        }
        this.user = JSON.parse(userData);
        this.loadAccountData();
    }

    loadAccountData() {
        this.api.getAccount(this.user.accountId).subscribe({
            next: (data) => {
                this.account = data;
            },
            error: (err) => {
                console.error('Error loading account:', err);
            }
        });
    }

    goBack() {
        this.router.navigate(['/dashboard']);
    }

    logout() {
        localStorage.clear();
        this.router.navigate(['/']);
    }
}
