import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { HostListener } from '@angular/core';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})

export class NavbarComponent implements OnInit {
  menuOpen = false;
  categoriasOpen = false;

  categorias = [
    { nombre: 'Conciertos', valor: 'CONCIERTO', icono: 'bi-music-note-beamed' },
    { nombre: 'Teatro', valor: 'TEATRO', icono: 'bi-mask' },
    { nombre: 'Deportes', valor: 'DEPORTE', icono: 'bi-trophy' },
    { nombre: 'Cursos', valor: 'CURSO', icono: 'bi-book' },
    { nombre: 'Conferencias', valor: 'CONFERENCIA', icono: 'bi-mic' },
    { nombre: 'Festivales', valor: 'FESTIVAL', icono: 'bi-stars' },
    { nombre: 'Talleres', valor: 'TALLER', icono: 'bi-tools' },
    { nombre: 'Otros', valor: 'OTRO', icono: 'bi-grid-3x3-gap' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit() { }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get userName(): string {
    const user = this.authService.getUser();
    return user ? (user.nombre ?? user.email ?? '') : '';
  }

  get userRol(): string {
    return this.authService.getRol() ?? '';
  }

  logout() {
    this.authService.logout();
  }

  getDashboardRoute(): string {
    if (this.userRol === 'ADMIN') return '/admin';
    if (this.userRol === 'ORGANIZADOR') return '/mis-eventos';
    return '/dashboard';
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }

  toggleCategorias() {
    this.categoriasOpen = !this.categoriasOpen;
  }

  seleccionarCategoria(categoria: string) {
    this.categoriasOpen = false;

    this.router.navigate(['/eventos'], {
      queryParams: {
        categoria: categoria
      }
    });
  }

  @HostListener('document:click', ['$event'])
  clickFuera(event: MouseEvent) {

    const elemento = event.target as HTMLElement;

    if (!elemento.closest('.dropdown-categorias')) {

      this.categoriasOpen = false;

    }

  }

}