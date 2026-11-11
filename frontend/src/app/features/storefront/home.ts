import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Product } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './home.html',
  styleUrls: ['./home.css']
})
export class HomeComponent implements OnInit {
  private readonly productsApi = inject(ProductService);
  readonly products = signal<Product[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    this.productsApi.getAll().subscribe({
      next: data => { this.products.set(data.slice(0, 4)); this.loading.set(false); },
      error: err => { this.error.set(err.userMessage || 'No fue posible cargar productos. Inicia la API.'); this.loading.set(false); }
    });
  }
}
