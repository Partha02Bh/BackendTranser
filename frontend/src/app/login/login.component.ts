import { Component } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  // USER fields
  userUsername = '';
  userPassword = '';
  userOtp = '';

  // ADMIN fields
  adminUsername = '';
  adminPassword = '';
  adminOtp = '';

  step = 1;  // 1 = credentials, 2 = OTP
  isLoading = false;
  errorMessage = '';
  selectedRole: 'USER' | 'OWNER' | null = null;
  pendingUsername = '';  // Store username for OTP verification

  // OTP Popup
  showOtpPopup = false;
  displayedOtp = '';
  otpCountdown = 10;
  private countdownInterval: any;

  constructor(private api: ApiService, private router: Router) { }

  /**
   * Step 1: Submit credentials and request OTP
   */
  selectRoleAndLogin(role: 'USER' | 'OWNER') {
    this.selectedRole = role;
    this.errorMessage = '';
    this.isLoading = true;

    const username = role === 'USER' ? this.userUsername : this.adminUsername;
    const password = role === 'USER' ? this.userPassword : this.adminPassword;

    if (!username || !password) {
      this.errorMessage = 'Please enter username and password';
      this.isLoading = false;
      return;
    }

    this.api.login(username, password, this.selectedRole!).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.pendingUsername = response.username;
        this.step = 2;

        // Show OTP popup for 10 seconds
        this.showOtpPopupWithCountdown(response.otp);
      },
      error: (err) => {
        this.isLoading = false;
        console.error('Login error:', err);
        if (err.status === 401) {
          this.errorMessage = 'Invalid username or password';
        } else {
          this.errorMessage = err.error?.message || 'Login failed. Please try again.';
        }
      }
    });
  }

  /**
   * Step 2: Verify OTP and get JWT
   */
  onVerify() {
    this.errorMessage = '';
    this.isLoading = true;

    const otp = this.selectedRole === 'USER' ? this.userOtp : this.adminOtp;

    if (!otp) {
      this.errorMessage = 'Please enter OTP';
      this.isLoading = false;
      return;
    }

    this.api.verifyOtp(this.pendingUsername, otp).subscribe({
      next: (response) => {
        this.isLoading = false;
        console.log('Login successful:', response);

        // Navigate based on backend response role
        if (response.role === 'OWNER' || response.role === 'ADMIN') {
          this.router.navigate(['/owner']);
        } else {
          this.router.navigate(['/dashboard']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.error('OTP verification error:', err);
        if (err.status === 401) {
          this.errorMessage = 'Invalid or expired OTP';
        } else {
          this.errorMessage = err.error?.message || 'Verification failed. Please try again.';
        }
      }
    });
  }

  /**
   * Go back to credentials step
   */
  goBack() {
    this.step = 1;
    this.userOtp = '';
    this.adminOtp = '';
    this.errorMessage = '';
    this.closeOtpPopup();
  }

  /**
   * Show OTP popup with 10-second countdown
   */
  showOtpPopupWithCountdown(otp: string) {
    this.displayedOtp = otp;
    this.otpCountdown = 10;
    this.showOtpPopup = true;

    // Clear any existing interval
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
    }

    // Start countdown
    this.countdownInterval = setInterval(() => {
      this.otpCountdown--;
      if (this.otpCountdown <= 0) {
        this.closeOtpPopup();
      }
    }, 1000);
  }

  /**
   * Close OTP popup
   */
  closeOtpPopup() {
    this.showOtpPopup = false;
    this.displayedOtp = '';
    if (this.countdownInterval) {
      clearInterval(this.countdownInterval);
      this.countdownInterval = null;
    }
  }
}
