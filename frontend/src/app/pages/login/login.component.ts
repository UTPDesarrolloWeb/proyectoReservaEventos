import { Component, NgZone, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { environment } from '../../../environments/environment';

declare var google: any;

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements AfterViewInit {
  email = ''; password = ''; loading = false; error = '';
  mfaRequired = false; otpCode = '';
  constructor(
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone
  ) { }
  onSubmit() {
    if (!this.email || !this.password) return;
    this.loading = true; this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: (response) => {
        if (response && response.mfaRequired) {
          this.mfaRequired = true;
          this.loading = false;
        } else {
          this.navigateByRole();
        }
      },
      error: (err) => {
        this.error = err.error?.mensaje || err.error?.message || 'Credenciales incorrectas.';
        this.loading = false;
      }
    });
  }
  onVerifyOtp() {
    if (!this.otpCode) return;
    this.loading = true; this.error = '';
    this.authService.verify2fa(this.email, this.otpCode).subscribe({
      next: () => {
        this.navigateByRole();
      },
      error: (err) => {
        this.error = err.error?.mensaje || err.error?.message || 'Código incorrecto o expirado.';
        this.loading = false;
      }
    });
  }
  cancelMfa() {
    this.mfaRequired = false;
    this.otpCode = '';
    this.password = '';
    this.error = '';
  }
  ngAfterViewInit() {
    this.initGoogleSignIn();
  }

  private initGoogleSignIn() {
    if (typeof google !== 'undefined') {
      google.accounts.id.initialize({
        client_id: environment.googleClientId,
        callback: this.handleCredentialResponse.bind(this),
        use_fedcm_for_prompt: false
      });
      google.accounts.id.renderButton(
        document.getElementById('google-btn'),
        { theme: 'outline', size: 'large', width: 350, use_fedcm_for_button: false }
      );
    } else {
      setTimeout(() => this.initGoogleSignIn(), 500);
    }
  }

  private handleCredentialResponse(response: any) {
    this.ngZone.run(() => {
      const idToken = response.credential;
      this.loading = true;
      this.error = '';
      this.authService.loginGoogle(idToken).subscribe({
        next: (res) => {
          if (res && res.mfaRequired) {
            // Google login requiere verificación de 2 pasos
            this.mfaRequired = true;
            this.email = res.email;
            this.loading = false;
          } else {
            this.navigateByRole();
          }
        },
        error: (err) => {
          this.error = err.error?.mensaje || err.error?.message || 'Error al iniciar sesión con Google.';
          this.loading = false;
        }
      });
    });
  }

  private navigateByRole() {
    const rol = this.authService.getRol();
    if (rol === 'ADMIN') this.router.navigate(['/admin']);
    else if (rol === 'ORGANIZADOR') this.router.navigate(['/mis-eventos']);
    else this.router.navigate(['/dashboard']);
  }
}