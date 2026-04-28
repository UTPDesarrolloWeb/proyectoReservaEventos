import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  email = ''; password = ''; loading = false; error = '';
  constructor(private authService: AuthService, private router: Router) {}
  onSubmit() {
    if (!this.email || !this.password) return;
    this.loading = true; this.error = '';
    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        const rol = this.authService.getRol();
        if (rol === 'ADMIN') this.router.navigate(['/admin']);
        else if (rol === 'ORGANIZADOR') this.router.navigate(['/mis-eventos']);
        else this.router.navigate(['/dashboard']);
      },
      error: () => { this.error = 'Credenciales incorrectas.'; this.loading = false; }
    });
  }
}