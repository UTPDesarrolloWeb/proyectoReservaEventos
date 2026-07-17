import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EventoService } from '../../services/evento.service';
import { ReservaService } from '../../services/reserva.service';
import { AuthService } from '../../services/auth.service';
import { Evento } from '../../models/evento.model';

@Component({
    selector: 'app-evento-detalle',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './evento-detalle.component.html',
    styleUrl: './evento-detalle.component.css'
})
export class EventoDetalleComponent implements OnInit {

    evento: Evento | null = null;
    loading = true;
    error = '';
    reservando = false;
    reservaExito = false;
    reservaError = '';
    cantidadEntradas = 1;

    mostrarModalPago = false;
    reservaCreada: any = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private eventoService: EventoService,
        private reservaService: ReservaService,
        private authService: AuthService
    ) { }

    ngOnInit() {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        if (!id) { this.router.navigate(['/eventos']); return; }
        this.cargarEvento(id);
    }

    cargarEvento(id: number) {
        this.loading = true;
        this.eventoService.verEvento(id).subscribe({
            next: (data) => { this.evento = data; this.loading = false; },
            error: () => { this.error = 'No se pudo cargar el evento.'; this.loading = false; }
        });
    }

    get isLoggedIn(): boolean { return this.authService.isLoggedIn(); }
    get isCliente(): boolean { return this.authService.getRol() === 'CLIENTE'; }

    reservar() {
        if (!this.evento) return;
        if (!this.isLoggedIn) { this.router.navigate(['/login']); return; }

        this.reservando = true;
        this.reservaError = '';

        this.reservaService.crearReserva(this.evento.id, this.cantidadEntradas).subscribe({
            next: (data: any) => {
                this.reservaCreada = data;
                this.mostrarModalPago = true;
                this.reservaExito = true;
                this.reservando = false;
            },
            error: (err) => {
                this.reservaError = err?.error?.message ?? 'Error al realizar la reserva.';
                this.reservando = false;
            }
        });
    }

    cerrarModal() {
        this.mostrarModalPago = false;
    }

    procesarPagoMock() {
        console.log('Procesando pago para la reserva :', this.reservaCreada.id);
        this.mostrarModalPago = false;
        this.router.navigate(['/mis-reservas']);
    }

    formatearFecha(fecha: string): string {
        return new Date(fecha).toLocaleDateString('es-PE', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
        });
    }

    formatearHora(fecha: string): string {
        return new Date(fecha).toLocaleTimeString('es-PE', {
            hour: '2-digit', minute: '2-digit'
        });
    }

    formatearPrecio(precio: number): string {
        return precio === 0 ? 'Gratis' : `S/ ${precio.toFixed(2)}`;
    }

    categoriaLabel(cat: string): string {
        const labels: Record<string, string> = {
            MUSICA: 'Música', DEPORTES: 'Deportes',
            TEATRO: 'Teatro', CONFERENCIA: 'Conferencia',
            GASTRONOMIA: 'Gastronomía', ARTE: 'Arte',
            TECNOLOGIA: 'Tecnología', OTRO: 'Otro'
        };
        return labels[cat] ?? cat;
    }

    get totalPagar(): number {
        return (this.evento?.precio ?? 0) * this.cantidadEntradas;
    }

    get maxEntradas(): number {
        return Math.min(this.evento?.aforoDisponible ?? 1, 10);
    }
}