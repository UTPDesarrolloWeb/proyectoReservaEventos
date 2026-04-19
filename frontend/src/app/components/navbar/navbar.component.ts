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
  isLoggedIn = false;
  userName   = '';
  userRol    = '';
  menuOpen   = false;

  constructor(private authService: AuthService) {}

  ngOnInit() {
    this.isLoggedIn = this.authService.isLoggedIn();
    const user = this.authService.getUser();
    if (user) {
      this.userName = user.nombre ?? user.email ?? '';
      this.userRol  = user.rol ?? '';
    }
  }

  logout() {
    this.authService.logout();
    this.isLoggedIn = false;
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