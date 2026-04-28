import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-autorizado',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './no-autorizado.component.html',
  styleUrl: './no-autorizado.component.css'
})
export class NoAutorizadoComponent {}