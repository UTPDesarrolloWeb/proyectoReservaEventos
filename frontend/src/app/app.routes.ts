import { Routes } from '@angular/router';
import { authGuard, adminGuard, organizadorGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./pages/register/register.component').then(m => m.RegisterComponent)
  },
  {
    path: 'eventos',
    loadComponent: () =>
      import('./pages/eventos-publicos/eventos-publicos.component').then(m => m.EventosPublicosComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'reservas',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/reservas/reservas.component').then(m => m.ReservasComponent)
  },
  {
    path: 'perfil',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/perfil/perfil.component').then(m => m.PerfilComponent)
  },
  {
    path: 'mis-eventos',
    canActivate: [organizadorGuard],
    loadComponent: () =>
      import('./pages/mis-eventos/mis-eventos.component').then(m => m.MisEventosComponent)
  },
  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./pages/admin/admin.component').then(m => m.AdminComponent)
  },
  {
    path: 'no-autorizado',
    loadComponent: () =>
      import('./pages/no-autorizado/no-autorizado.component').then(m => m.NoAutorizadoComponent)
  },
  { path: '**', redirectTo: '' }
];