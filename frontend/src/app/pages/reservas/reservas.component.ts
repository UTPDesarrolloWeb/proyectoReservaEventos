import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReservaService } from '../../services/reserva.service';
import { Reserva } from '../../models/reserva.model';

@Component({
  selector: 'app-reservas',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './reservas.component.html',
  styleUrl: './reservas.component.css'
})
export class ReservasComponent implements OnInit {

  reservas: Reserva[] = [];
  reservasFiltradas: Reserva[] = [];
  loading = true;
  error = '';
  filtroActivo: 'TODAS' | 'CONFIRMADA' | 'PENDIENTE' | 'CANCELADA' = 'TODAS';

  cancelando: number | null = null;
  cancelError = '';

  mostrarQR: Reserva | null = null;

  constructor(private reservaService: ReservaService) { }

  ngOnInit() {
    this.cargarReservas();
  }

  cargarReservas() {
    this.loading = true;
    this.error = '';
    this.reservaService.misReservas().subscribe({
      next: (data) => {
        this.reservas = data;
        this.reservasFiltradas = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar tus reservas.';
        this.loading = false;
      }
    });
  }

  filtrar(estado: 'TODAS' | 'CONFIRMADA' | 'PENDIENTE' | 'CANCELADA') {
    this.filtroActivo = estado;
    this.reservasFiltradas = estado === 'TODAS'
      ? this.reservas
      : this.reservas.filter(r => r.estado === estado);
  }

  cancelar(reserva: Reserva) {
    if (!confirm('¿Estás seguro de cancelar esta reserva?')) return;
    this.cancelando = reserva.id;
    this.cancelError = '';

    this.reservaService.cancelarReserva(reserva.id).subscribe({
      next: (actualizada) => {
        const idx = this.reservas.findIndex(r => r.id === reserva.id);
        if (idx !== -1) this.reservas[idx] = actualizada;
        this.filtrar(this.filtroActivo);
        this.cancelando = null;
      },
      error: (err) => {
        this.cancelError = err?.error?.message ?? 'Error al cancelar la reserva.';
        this.cancelando = null;
      }
    });
  }

  verQR(reserva: Reserva) {
    this.mostrarQR = reserva;
  }

  cerrarQR() {
    this.mostrarQR = null;
  }

  formatearFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-PE', {
      day: 'numeric', month: 'long', year: 'numeric'
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

  get totalConfirmadas(): number { return this.reservas.filter(r => r.estado === 'CONFIRMADA').length; }
  get totalPendientes(): number { return this.reservas.filter(r => r.estado === 'PENDIENTE').length; }
  get totalCanceladas(): number { return this.reservas.filter(r => r.estado === 'CANCELADA').length; }
}