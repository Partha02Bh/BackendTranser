import { Component } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user = '';
  pass = '';
  otp = '';
  step = 1 ;

  constructor(private api: ApiService, private router: Router){}

  onLogin(){
    this.api.login(this.user, this.pass).subscribe({
      next: (res)=>{
        alert("OTP Generated: " + res);
        this.step = 2;
      },
      error: () => alert("Invalid Credentials")
    });
  }
  onVerify(){
    this.api.verify(this.user, this.otp).subscribe({
      next: (res)=>{

        localStorage.setItem('user', JSON.stringify(res));
        if(res.role === 'OWNER'){
          this.router.navigate(['/owner']);
        } else{
          this.router.navigate(['/dashboard'])
        }
      },
      error: () => alert("Invalid OTP")
    });
  }
}
