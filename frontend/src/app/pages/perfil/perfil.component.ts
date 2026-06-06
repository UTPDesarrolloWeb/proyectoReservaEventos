import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './perfil.component.html',
  styleUrl: './perfil.component.css'
})
export class PerfilComponent implements OnInit {
  nombre = '';
  apellido = '';
  email = '';
  rol = '';
  
  loading = false;
  saving = false;
  error = '';
  success = '';

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.cargarPerfil();
  }

  cargarPerfil() {
    this.loading = true;
    this.error = '';
    this.authService.getPerfil().subscribe({
      next: (data) => {
        this.nombre = data.nombre || '';
        this.apellido = data.apellido || '';
        this.email = data.email || '';
        this.rol = data.rol || '';
        this.loading = false;
      },
      error: (err) => {
        this.error = 'No se pudo cargar el perfil del usuario.';
        this.loading = false;
      }
    });
  }

  onSave() {
    if (!this.nombre || !this.apellido) return;
    this.saving = true;
    this.error = '';
    this.success = '';
    
    this.authService.updatePerfil({ nombre: this.nombre, apellido: this.apellido }).subscribe({
      next: (data) => {
        this.nombre = data.nombre || '';
        this.apellido = data.apellido || '';
        this.success = 'Perfil actualizado exitosamente.';
        this.saving = false;
      },
      error: (err) => {
        this.error = err.error?.mensaje || err.error?.message || 'Error al actualizar el perfil.';
        this.saving = false;
      }
    });
  }

  getRoleLabel(role: string): string {
    if (role === 'ADMIN') return 'Administrador';
    if (role === 'ORGANIZADOR') return 'Organizador';
    return 'Asistente';
  }
}