import { Component, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { HeaderComponent } from './shared/components/header';
import { FooterComponent } from './shared/components/footer';
import { ApiStatusComponent } from './shared/components/api-status';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, HeaderComponent, FooterComponent, ApiStatusComponent],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  private readonly router = inject(Router);
  readonly authScreen = signal(true);
  constructor() {
    this.router.events.pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe(event => {
        const path = event.urlAfterRedirects.split('?')[0];
        this.authScreen.set(path === '/acceso' || path === '/cuenta');
      });
  }
}
