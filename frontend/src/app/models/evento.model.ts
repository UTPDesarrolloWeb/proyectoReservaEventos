export type CategoriaEvento =
    'MUSICA' | 'DEPORTES' | 'TEATRO' | 'CONFERENCIA' |
    'GASTRONOMIA' | 'ARTE' | 'TECNOLOGIA' | 'OTRO';

export type EstadoEvento = 'BORRADOR' | 'PUBLICADO' | 'CANCELADO';

export interface Organizador {
    id: number;
    usuarioId: number;
    eventosCreados: number;
}

export interface Evento {
    id: number;
    titulo: string;
    descripcion: string;
    fechaEvento: string;
    lugar: string;
    aforo: number;
    aforoDisponible: number;
    precio: number;
    imagenUrl?: string;
    categoria: CategoriaEvento;
    estado: EstadoEvento;
    fechaCreacion: string;
    organizador: Organizador;
}

export interface EventoFiltros {
    titulo?: string;
    lugar?: string;
    categoria?: CategoriaEvento;
}

export interface EventoRequest {
    titulo: string;
    descripcion: string;
    fechaEvento: string;
    lugar: string;
    aforo: number;
    precio: number;
    imagenUrl?: string;
    categoria: CategoriaEvento;
}