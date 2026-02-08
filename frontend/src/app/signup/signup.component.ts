import { Component } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
    selector: 'app-signup',
    templateUrl: './signup.component.html',
    styleUrls: ['./signup.component.css']
})
export class SignupComponent {
    fullName = '';
    username = '';
    password = '';
    initialBalance: number | null = null;
    loading = false;
    showPassword = false;

    constructor(private api: ApiService, private router: Router) { }

    register() {
        if (!this.username || !this.password || !this.fullName) {
            alert('Please fill in all required fields');
            return;
        }

        this.loading = true;
        const payload = {
            username: this.username,
            password: this.password,
            holderName: this.fullName,
            initialBalance: this.initialBalance || 0
        };

        this.api.register(payload).subscribe({
            next: () => {
                alert('Account created successfully! Please login.');
                this.router.navigate(['/login']);
            },
            error: (err) => {
                alert(err.error?.message || 'Registration failed');
                this.loading = false;
            }
        });
    }
}
