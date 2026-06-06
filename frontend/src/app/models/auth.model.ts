export type Rol = 'ADMIN' | 'ORGANIZADOR' | 'CLIENTE';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombre: string;
  apellido: string;
  email: string;
  password: string;
  rol: Rol;
}

export interface AuthResponse {
  token: string;
  email: string;
  nombre?: string;
  apellido?: string;
  rol?: Rol;
  [key: string]: any;
}