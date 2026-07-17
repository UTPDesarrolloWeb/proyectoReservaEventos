import { Component, NgZone, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Rol } from '../../models/auth.model';
import { environment } from '../../../environments/environment';

declare var google: any;

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent implements AfterViewInit {
  nombre = ''; apellido = ''; email = ''; password = ''; confirmPassword = ''; rol: Rol = 'CLIENTE';
  loading = false; error = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private ngZone: NgZone
  ) { }

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
        document.getElementById('google-btn-register'),
        { theme: 'outline', size: 'large', width: 350, text: 'signup_with', use_fedcm_for_button: false }
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
        next: () => {
          this.navigateByRole();
        },
        error: (err) => {
          this.error = err.error?.mensaje || err.error?.message || 'Error al registrarse con Google.';
          this.loading = false;
        }
      });
    });
  }
  // private handleCredentialResponse(response: any) {
  //   console.log("TOKEN GOOGLE:", response.credential);
  // }
  private navigateByRole() {
    const rol = this.authService.getRol();
    if (rol === 'ADMIN') this.router.navigate(['/admin']);
    else if (rol === 'ORGANIZADOR') this.router.navigate(['/mis-eventos']);
    else this.router.navigate(['/dashboard']);
  }

  onSubmit() {
    this.error = '';
    if (this.password !== this.confirmPassword) { this.error = 'Las contraseñas no coinciden.'; return; }
    if (this.password.length < 6) { this.error = 'Mínimo 6 caracteres.'; return; }
    this.loading = true;
    this.authService.register({ nombre: this.nombre, apellido: this.apellido, email: this.email, password: this.password, rol: this.rol }).subscribe({
      next: () => { this.router.navigate(this.rol === 'ORGANIZADOR' ? ['/mis-eventos'] : ['/dashboard']); },
      error: (err) => {
        this.error = err.error?.mensaje || err.error?.message || 'Error al crear la cuenta.';
        this.loading = false;
      }
    });
  }
}