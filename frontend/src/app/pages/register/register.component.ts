import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Rol } from '../../models/auth.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  nombre = ''; apellido = ''; email = ''; password = ''; confirmPassword = ''; rol: Rol = 'CLIENTE';
  loading = false; error = '';
  constructor(private authService: AuthService, private router: Router) { }
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