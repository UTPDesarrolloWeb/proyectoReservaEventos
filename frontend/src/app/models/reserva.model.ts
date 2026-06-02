export type EstadoReserva = 'PENDIENTE' | 'CONFIRMADA' | 'CANCELADA';

export interface Reserva {
    id: number;
    clienteId: number;
    eventoId: number;
    fechaReserva: string;
    estado: EstadoReserva;
    codigoQR?: string;
    cantidadEntradas: number;
    montoTotal: number;
}

export interface ReservaConEvento extends Reserva {
    evento?: {
        titulo: string;
        fechaEvento: string;
        lugar: string;
        imagenUrl?: string;
    };
}