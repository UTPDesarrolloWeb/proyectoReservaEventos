import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { EventoService } from '../../services/evento.service';
import { Evento, EventoRequest, CategoriaEvento } from '../../models/evento.model';

@Component({
  selector: 'app-mis-eventos',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mis-eventos.component.html',
  styleUrl: './mis-eventos.component.css'
})
export class MisEventosComponent implements OnInit {

  eventos: Evento[] = [];
  eventosFiltrados: Evento[] = [];
  estadisticas: any = null;

  loading = true;
  loadingEstadisticas = true;
  error = '';

  filtroEstado = 'TODOS';

  // Modal crear/editar
  mostrarModal = false;
  editandoId: number | null = null;
  guardando = false;
  modalError = '';

  form: EventoRequest = this.formVacio();

  categorias: CategoriaEvento[] = [
    'CONCIERTO', 'TALLER', 'CONFERENCIA', 'CURSO',
    'FESTIVAL', 'DEPORTE', 'TEATRO', 'OTRO'
  ];

  // Acciones
  procesando: number | null = null;

  constructor(private eventoService: EventoService) { }

  ngOnInit() {
    this.cargarEventos();
    this.cargarEstadisticas();
  }

  cargarEventos() {
    this.loading = true;
    this.eventoService.misEventos().subscribe({
      next: (data) => { this.eventos = data; this.aplicarFiltro(); this.loading = false; },
      error: () => { this.error = 'No se pudieron cargar tus eventos.'; this.loading = false; }
    });
  }

  cargarEstadisticas() {
    this.loadingEstadisticas = true;
    this.eventoService.misEstadisticas().subscribe({
      next: (data) => { this.estadisticas = data; this.loadingEstadisticas = false; },
      error: () => { this.loadingEstadisticas = false; }
    });
  }

  aplicarFiltro() {
    this.eventosFiltrados = this.filtroEstado === 'TODOS'
      ? this.eventos
      : this.eventos.filter(e => e.estado === this.filtroEstado);
  }

  filtrar(estado: string) {
    this.filtroEstado = estado;
    this.aplicarFiltro();
  }

  // ── Modal crear ──────────────────────────────────────
  abrirCrear() {
    this.editandoId = null;
    this.form = this.formVacio();
    this.modalError = '';
    this.mostrarModal = true;
  }

  abrirEditar(evento: Evento) {
    this.editandoId = evento.id;
    this.form = {
      titulo: evento.titulo,
      descripcion: evento.descripcion,
      fechaEvento: evento.fechaEvento.substring(0, 16),
      lugar: evento.lugar,
      aforo: evento.aforo,
      precio: evento.precio,
      imagenUrl: evento.imagenUrl ?? '',
      categoria: evento.categoria
    };
    this.modalError = '';
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.editandoId = null;
    this.modalError = '';
  }

  guardar() {
    this.guardando = true;
    this.modalError = '';

    const obs = this.editandoId
      ? this.eventoService.editarEvento(this.editandoId, this.form)
      : this.eventoService.crearEvento(this.form);

    obs.subscribe({
      next: () => {
        this.guardando = false;
        this.cerrarModal();
        this.cargarEventos();
      },
      error: (err) => {
        this.modalError = err?.error?.message ?? 'Error al guardar el evento.';
        this.guardando = false;
      }
    });
  }

  // ── Acciones ─────────────────────────────────────────
  publicar(evento: Evento) {
    this.procesando = evento.id;
    this.eventoService.publicarEvento(evento.id).subscribe({
      next: () => { this.cargarEventos(); this.procesando = null; },
      error: () => { this.procesando = null; }
    });
  }

  cancelar(evento: Evento) {
    if (!confirm(`¿Cancelar el evento "${evento.titulo}"?`)) return;
    this.procesando = evento.id;
    this.eventoService.cancelarEvento(evento.id).subscribe({
      next: () => { this.cargarEventos(); this.procesando = null; },
      error: () => { this.procesando = null; }
    });
  }

  // ── Helpers ───────────────────────────────────────────
  formVacio(): EventoRequest {
    return {
      titulo: '', descripcion: '', fechaEvento: '',
      lugar: '', aforo: 1, precio: 0,
      imagenUrl: '', categoria: 'OTRO'
    };
  }

  formatearFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-PE', {
      day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });
  }

  formatearPrecio(precio: number): string {
    return precio === 0 ? 'Gratis' : `S/ ${precio.toFixed(2)}`;
  }

  estadoClass(estado: string): string {
    const map: Record<string, string> = {
      PUBLICADO: 'badge-success',
      BORRADOR: 'badge-default',
      CANCELADO: 'badge-danger'
    };
    return map[estado] ?? 'badge-default';
  }

  estadoLabel(estado: string): string {
    const map: Record<string, string> = {
      PUBLICADO: 'Publicado',
      BORRADOR: 'Borrador',
      CANCELADO: 'Cancelado'
    };
    return map[estado] ?? estado;
  }

  categoriaLabel(cat: string): string {
    const labels: Record<string, string> = {
      CONCIERTO: 'Concierto', TALLER: 'Taller',
      CONFERENCIA: 'Conferencia', CURSO: 'Curso',
      FESTIVAL: 'Festival', DEPORTE: 'Deporte',
      TEATRO: 'Teatro', OTRO: 'Otro'
    };
    return labels[cat] ?? cat;
  }

  get totalPublicados(): number { return this.eventos.filter(e => e.estado === 'PUBLICADO').length; }
  get totalBorradores(): number { return this.eventos.filter(e => e.estado === 'BORRADOR').length; }
  get totalCancelados(): number { return this.eventos.filter(e => e.estado === 'CANCELADO').length; }
}