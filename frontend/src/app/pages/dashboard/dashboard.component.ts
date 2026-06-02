import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ReservaService } from '../../services/reserva.service';
import { EventoService } from '../../services/evento.service';
import { Reserva } from '../../models/reserva.model';
import { Evento } from '../../models/evento.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  userName = '';
  reservas: Reserva[] = [];
  recomendados: Evento[] = [];

  loadingReservas = true;
  loadingRecomendados = true;

  constructor(
    private authService: AuthService,
    private reservaService: ReservaService,
    private eventoService: EventoService
  ) { }

  ngOnInit() {
    const user = this.authService.getUser();
    this.userName = user?.nombre ?? user?.email ?? 'Usuario';
    this.cargarReservas();
    this.cargarRecomendados();
  }

  cargarReservas() {
    this.loadingReservas = true;
    this.reservaService.misReservas().subscribe({
      next: (data) => { this.reservas = data.slice(0, 3); this.loadingReservas = false; },
      error: () => { this.loadingReservas = false; }
    });
  }

  cargarRecomendados() {
    this.loadingRecomendados = true;
    this.eventoService.recomendados().subscribe({
      next: (data) => { this.recomendados = data.slice(0, 4); this.loadingRecomendados = false; },
      error: () => {
        // Si falla recomendados, cargamos públicos como fallback
        this.eventoService.listarPublicos().subscribe({
          next: (data) => { this.recomendados = data.slice(0, 4); this.loadingRecomendados = false; },
          error: () => { this.loadingRecomendados = false; }
        });
      }
    });
  }

  get totalReservas(): number { return this.reservas.length; }
  get reservasActivas(): number { return this.reservas.filter(r => r.estado === 'CONFIRMADA').length; }
  get reservasPendientes(): number { return this.reservas.filter(r => r.estado === 'PENDIENTE').length; }

  formatearFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-PE', {
      day: 'numeric', month: 'short', year: 'numeric'
    });
  }

  formatearPrecio(precio: number): string {
    return precio === 0 ? 'Gratis' : `S/ ${precio.toFixed(2)}`;
  }

  estadoBadgeClass(estado: string): string {
    const map: Record<string, string> = {
      CONFIRMADA: 'badge-success',
      PENDIENTE: 'badge-warning',
      CANCELADA: 'badge-danger'
    };
    return map[estado] ?? 'badge-default';
  }

  estadoLabel(estado: string): string {
    const map: Record<string, string> = {
      CONFIRMADA: 'Confirmada',
      PENDIENTE: 'Pendiente',
      CANCELADA: 'Cancelada'
    };
    return map[estado] ?? estado;
  }
}