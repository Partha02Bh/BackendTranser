import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-otp',
  templateUrl: './otp.component.html'
})
export class OtpComponent {

  otp = '';

  constructor(private http: HttpClient, private router: Router) { }

  verifyOtp() {
    const role = localStorage.getItem('role');

    this.http.post('http://localhost:8080/auth/verify-otp', { otp: this.otp })
      .subscribe({
        next: () => {
          role === 'USER'
            ? this.router.navigate(['/dashboard'])
            : this.router.navigate(['/owner']);
        },
        error: () => alert('Invalid OTP')
      });
  }
}
