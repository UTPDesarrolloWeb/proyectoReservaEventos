import { HttpClient, HttpParams } from "@angular/common/http";
import { environment } from "../../environments/environment";
import { Observable } from "rxjs";

export class AdminService {

    private readonly api = `${environment.apiUrl}/admin`


    constructor(private http: HttpClient) { }

    getDashborad(): Observable<any> {
        return this.http.get<any>(`${this.api}/dashboard`);
    }

    listarUsuarios(): Observable<any[]> {
        return this.http.get<any[]>(`${this.api}/usuarios`);
    }

    toogleUsuario(id: number): Observable<any> {
        return this.http.put<any>(`${this.api}/usuarios/${id}/toggle`, {});
    }

    historialPagos(pagina = 0, cantidad = 10): Observable<any> {
        const params = new HttpParams()
            .set('pagina', pagina)
            .set('cantidad', cantidad);
        return this.http.get<any>(`${this.api}/pagos`, { params });
    }

    ingresosPorPeriodo(periodo: 'mes' | 'semana' | 'anio' = 'mes'): Observable<any> {
        const params = new HttpParams().set('periodo', periodo);
        return this.http.get<any>(`${this.api}/ingresos`, { params });
    }

    eventosPorEstado(estado = 'PUBLICADO', pagina = 0, cantidad = 10): Observable<any> {
        const params = new HttpParams()
            .set('estado', estado)
            .set('pagina', pagina)
            .set('cantidad', cantidad);
        return this.http.get<any>(`${this.api}/eventos`, { params })
    }

    organizadoresPorEstadoPlan(): Observable<any> {
        return this.http.get<any>(`${this.api}/organizadores/estado-plan`);
    }

}