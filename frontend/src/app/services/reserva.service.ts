import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Reserva } from '../models/reserva.model';

@Injectable({ providedIn: 'root' })
export class ReservaService {

    private readonly api = `${environment.apiUrl}/reservas`;

    constructor(private http: HttpClient) { }

    crearReserva(eventoId: number, cantidadEntradas: number): Observable<Reserva> {
        const params = new HttpParams().set('cantidadEntradas', cantidadEntradas);
        return this.http.post<Reserva>(`${this.api}/evento/${eventoId}`, null, { params });
    }

    cancelarReserva(reservaId: number): Observable<Reserva> {
        return this.http.put<Reserva>(`${this.api}/${reservaId}/cancelar`, null);
    }

    misReservas(): Observable<Reserva[]> {
        return this.http.get<Reserva[]>(`${this.api}/mis-reservas`);
    }

    misReservasPaginadas(pagina: number, cantidad: number): Observable<any> {
        const params = new HttpParams()
            .set('pagina', pagina)
            .set('cantidad', cantidad);
        return this.http.get<any>(`${this.api}/mis-reservas/paginado`, { params });
    }

    reservasPorEvento(eventoId: number): Observable<Reserva[]> {
        return this.http.get<Reserva[]>(`${this.api}/evento/${eventoId}`);
    }
}
