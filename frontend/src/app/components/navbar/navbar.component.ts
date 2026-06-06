import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
  menuOpen   = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {}

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
    if (this.userRol === 'ADMIN')       return '/admin';
    if (this.userRol === 'ORGANIZADOR') return '/mis-eventos';
    return '/dashboard';
  }

  toggleMenu() {
    this.menuOpen = !this.menuOpen;
  }
}