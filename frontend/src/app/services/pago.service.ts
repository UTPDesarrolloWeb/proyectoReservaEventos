import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PagoService {

    private readonly api = `${environment.apiUrl}/pagos`;

    constructor(private http: HttpClient) { }

    crearSessionPago(reservaId: number): Observable<{ sessionId: string, sessionUrl: string }> {
        return this.http.post<{ sessionId: string, sessionUrl: string }>(
            `${this.api}/stripe/session/${reservaId}`,
            null
        );
    }

    procesarPago(reservaId: number, metodo: string): Observable<any> {
        const transaccionId = `stripe_${reservaId}_${Date.now()}`;
        return this.http.post(
            `${this.api}/reserva/${reservaId}?metodo=${metodo}&transaccionId=${transaccionId}`,
            null
        );
    }
}
