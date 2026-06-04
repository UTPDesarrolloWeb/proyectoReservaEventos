import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent implements OnInit {

  // Dashboard
  dashboard: any = null;
  loadingDash = true;

  // Usuarios
  usuarios: any[] = [];
  usuariosFiltrados: any[] = [];
  loadingUsuarios = false;
  busquedaUsuario = '';
  toggling: number | null = null;

  // Eventos
  eventos: any[] = [];
  loadingEventos = false;
  filtroEstadoEvento = 'PUBLICADO';

  // Ingresos
  ingresos: any = null;
  loadingIngresos = false;
  periodoIngresos: 'mes' | 'semana' | 'anio' = 'mes';

  // Tabs
  tabActivo: 'dashboard' | 'usuarios' | 'eventos' | 'ingresos' = 'dashboard';

  constructor(private adminService: AdminService) { }

  ngOnInit() {
    this.cargarDashboard();
  }

  // Tab switch 
  cambiarTab(tab: 'dashboard' | 'usuarios' | 'eventos' | 'ingresos') {
    this.tabActivo = tab;
    if (tab === 'usuarios' && this.usuarios.length === 0) this.cargarUsuarios();
    if (tab === 'eventos' && this.eventos.length === 0) this.cargarEventos();
    if (tab === 'ingresos' && !this.ingresos) this.cargarIngresos();
  }

  // Dashboard 
  cargarDashboard() {
    this.loadingDash = true;
    this.adminService.getDashborad().subscribe({
      next: (data) => { this.dashboard = data; this.loadingDash = false; },
      error: () => { this.loadingDash = false; }
    });
  }

  // Usuarios 
  cargarUsuarios() {
    this.loadingUsuarios = true;
    this.adminService.listarUsuarios().subscribe({
      next: (data) => {
        this.usuarios = data;
        this.usuariosFiltrados = data;
        this.loadingUsuarios = false;
      },
      error: () => { this.loadingUsuarios = false; }
    });
  }

  filtrarUsuarios() {
    const q = this.busquedaUsuario.toLowerCase();
    this.usuariosFiltrados = q
      ? this.usuarios.filter(u =>
        u.nombre?.toLowerCase().includes(q) ||
        u.email?.toLowerCase().includes(q) ||
        u.apellido?.toLowerCase().includes(q))
      : this.usuarios;
  }

  toggleUsuario(usuario: any) {
    this.toggling = usuario.id;
    this.adminService.toogleUsuario(usuario.id).subscribe({
      next: (actualizado) => {
        const idx = this.usuarios.findIndex(u => u.id === usuario.id);
        if (idx !== -1) this.usuarios[idx] = actualizado;
        this.filtrarUsuarios();
        this.toggling = null;
      },
      error: () => { this.toggling = null; }
    });
  }

  // Eventos 
  cargarEventos() {
    this.loadingEventos = true;
    this.adminService.eventosPorEstado(this.filtroEstadoEvento).subscribe({
      next: (data) => {
        this.eventos = data?.content ?? data ?? [];
        this.loadingEventos = false;
      },
      error: () => { this.loadingEventos = false; }
    });
  }

  cambiarFiltroEvento(estado: string) {
    this.filtroEstadoEvento = estado;
    this.cargarEventos();
  }

  // Ingresos 
  cargarIngresos() {
    this.loadingIngresos = true;
    this.adminService.ingresosPorPeriodo(this.periodoIngresos).subscribe({
      next: (data) => { this.ingresos = data; this.loadingIngresos = false; },
      error: () => { this.loadingIngresos = false; }
    });
  }

  cambiarPeriodo(periodo: 'mes' | 'semana' | 'anio') {
    this.periodoIngresos = periodo;
    this.cargarIngresos();
  }

  // Helpers 
  formatearFecha(fecha: string): string {
    if (!fecha) return '—';
    return new Date(fecha).toLocaleDateString('es-PE', {
      day: 'numeric', month: 'short', year: 'numeric'
    });
  }

  formatearPrecio(precio: number): string {
    if (!precio) return 'S/ 0.00';
    return `S/ ${precio.toFixed(2)}`;
  }

  rolBadgeClass(rol: string): string {
    const map: Record<string, string> = {
      ADMIN: 'badge-purple',
      ORGANIZADOR: 'badge-blue',
      CLIENTE: 'badge-gray',
      USUARIO: 'badge-gray'
    };
    return map[rol] ?? 'badge-gray';
  }

  estadoEventoClass(estado: string): string {
    const map: Record<string, string> = {
      PUBLICADO: 'badge-success',
      BORRADOR: 'badge-default',
      CANCELADO: 'badge-danger'
    };
    return map[estado] ?? 'badge-default';
  }

  // Getter para acceder a ingresos como objeto
  get ingresosObj(): any {
    return this.ingresos as any;
  }
}