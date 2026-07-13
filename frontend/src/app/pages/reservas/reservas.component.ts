import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { ReservaService } from '../../services/reserva.service';
import { PagoService } from '../../services/pago.service';
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

  pagandoId: number | null = null;
  pagoExitoMsg = '';
  pagoErrorMsg = '';

  constructor(
    private reservaService: ReservaService,
    private pagoService: PagoService,
    private route: ActivatedRoute
  ) { }

  ngOnInit() {
    this.cargarReservas();
    this.detectarPagoStatus();
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

  detectarPagoStatus() {
    this.route.queryParams.subscribe(params => {
      const status = params['payment_status'];
      const reservaId = params['reservaId'];
      if (status && reservaId) {
        if (status === 'success') {
          this.pagoService.procesarPago(reservaId, 'STRIPE').subscribe({
            next: () => {
              this.pagoExitoMsg = `¡Pago aprobado para la reserva #${reservaId}! Actualizando estado...`;
              // Polling: reintenta hasta 10 veces (cada 1s) hasta que la reserva cambie a CONFIRMADA
              let intentos = 0;
              const maxIntentos = 10;
              const intervalo = setInterval(() => {
                intentos++;
                this.cargarReservas();
                const reserva = this.reservas.find(r => r.id == reservaId);
                if ((reserva && reserva.estado === 'CONFIRMADA') || intentos >= maxIntentos) {
                  clearInterval(intervalo);
                  this.pagoExitoMsg = `¡Pago aprobado! Tu reserva #${reservaId} ha sido confirmada y se ha enviado la boleta a tu correo.`;
                }
              }, 1000);
            },
            error: (err) => {
              console.error('Error confirmando pago', err);
              this.pagoErrorMsg = 'El pago se realizó, pero hubo un problema al actualizar la reserva.';
            }
          });
        } else if (status === 'failure') {
          this.pagoErrorMsg = `El pago para la reserva #${reservaId} fue rechazado o cancelado. Por favor, intenta de nuevo.`;
        } else if (status === 'pending') {
          this.pagoExitoMsg = `El pago para la reserva #${reservaId} está en proceso. Te notificaremos una vez que sea aprobado.`;
        }
        window.history.replaceState({}, document.title, window.location.pathname);
      }
    });
  }

  pagarConStripe(reserva: Reserva) {
    this.pagandoId = reserva.id;
    this.pagoErrorMsg = '';
    this.pagoExitoMsg = '';

    this.pagoService.crearSessionPago(reserva.id).subscribe({
      next: (res) => {
        if (res && res.sessionUrl) {
          window.location.href = res.sessionUrl;
        } else {
          this.pagoErrorMsg = 'No se recibió la URL de pago de Stripe.';
          this.pagandoId = null;
        }
      },
      error: (err) => {
        this.pagoErrorMsg = err?.error?.message ?? 'Error al iniciar el pago con Stripe.';
        this.pagandoId = null;
      }
    });
  }
}