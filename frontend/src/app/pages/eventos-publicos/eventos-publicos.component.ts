import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { EventoService } from '../../services/evento.service';
import { Evento, CategoriaEvento } from '../../models/evento.model';

@Component({
  selector: 'app-eventos-publicos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './eventos-publicos.component.html',
  styleUrl: './eventos-publicos.component.css'
})
export class EventosPublicosComponent implements OnInit {

  eventos: Evento[] = [];
  eventosFiltrados: Evento[] = [];
  loading = true;
  error = '';

  busqueda = '';
  lugar = '';
  categoria = '';

  categorias: CategoriaEvento[] = [
    'MUSICA', 'DEPORTES', 'TEATRO', 'CONFERENCIA',
    'GASTRONOMIA', 'ARTE', 'TECNOLOGIA', 'OTRO'
  ];

  constructor(private eventoService: EventoService, private router: Router) { }

  ngOnInit() {
    this.cargarEventos();
  }

  cargarEventos() {
    this.loading = true;
    this.error = '';
    this.eventoService.listarPublicos().subscribe({
      next: (data) => {
        this.eventos = data;
        this.eventosFiltrados = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudieron cargar los eventos. Intenta más tarde.';
        this.loading = false;
      }
    });
  }

  filtrar() {
    if (this.busqueda || this.lugar || this.categoria) {
      this.eventoService.buscar({
        titulo: this.busqueda || undefined,
        lugar: this.lugar || undefined,
        categoria: (this.categoria || undefined) as CategoriaEvento | undefined
      }).subscribe({
        next: (data) => this.eventosFiltrados = data,
        error: () => this.filtrarLocal()
      });
    } else {
      this.eventosFiltrados = this.eventos;
    }
  }

  filtrarLocal() {
    this.eventosFiltrados = this.eventos.filter(e => {
      const matchTitulo = !this.busqueda || e.titulo.toLowerCase().includes(this.busqueda.toLowerCase());
      const matchLugar = !this.lugar || e.lugar.toLowerCase().includes(this.lugar.toLowerCase());
      const matchCategoria = !this.categoria || e.categoria === this.categoria;
      return matchTitulo && matchLugar && matchCategoria;
    });
  }

  limpiarFiltros() {
    this.busqueda = '';
    this.lugar = '';
    this.categoria = '';
    this.eventosFiltrados = this.eventos;
  }

  verDetalle(id: number) {
    this.router.navigate(['/eventos', id]);
  }

  formatearFecha(fecha: string): string {
    return new Date(fecha).toLocaleDateString('es-PE', {
      weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
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

  get hayFiltros(): boolean {
    return !!(this.busqueda || this.lugar || this.categoria);
  }
}