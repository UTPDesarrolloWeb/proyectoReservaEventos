import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Evento, EventoFiltros, EventoRequest } from '../models/evento.model';

@Injectable({ providedIn: 'root' })
export class EventoService {

    private readonly api = `${environment.apiUrl}/eventos`;

    constructor(private http: HttpClient) { }

    // ── PÚBLICOS ───────────────────────────────────────────
    listarPublicos(): Observable<Evento[]> {
        return this.http.get<Evento[]>(`${this.api}/publicos`);
    }

    verEvento(id: number): Observable<Evento> {
        return this.http.get<Evento>(`${this.api}/publicos/${id}`);
    }

    buscar(filtros: EventoFiltros): Observable<Evento[]> {
        let params = new HttpParams();
        if (filtros.titulo) params = params.set('titulo', filtros.titulo);
        if (filtros.lugar) params = params.set('lugar', filtros.lugar);
        if (filtros.categoria) params = params.set('categoria', filtros.categoria);
        return this.http.get<Evento[]>(`${this.api}/buscar`, { params });
    }

    recomendados(): Observable<Evento[]> {
        return this.http.get<Evento[]>(`${this.api}/recomendados`);
    }

    // ── ORGANIZADOR ────────────────────────────────────────
    misEventos(): Observable<Evento[]> {
        return this.http.get<Evento[]>(`${this.api}/mis-eventos`);
    }

    crearEvento(evento: EventoRequest): Observable<Evento> {
        return this.http.post<Evento>(this.api, evento);
    }

    editarEvento(id: number, evento: EventoRequest): Observable<Evento> {
        return this.http.put<Evento>(`${this.api}/${id}`, evento);
    }

    publicarEvento(id: number): Observable<Evento> {
        return this.http.put<Evento>(`${this.api}/${id}/publicar`, {});
    }

    cancelarEvento(id: number): Observable<Evento> {
        return this.http.put<Evento>(`${this.api}/${id}/cancelar`, {});
    }

    estadisticaEvento(id: number): Observable<any> {
        return this.http.get<any>(`${this.api}/${id}/estadistica`);
    }

    misEstadisticas(): Observable<any> {
        return this.http.get<any>(`${this.api}/mis-estadisticas`);
    }
}