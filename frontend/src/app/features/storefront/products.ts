import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Product } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';
import { CartService } from '../../core/services/cart.service';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.html',
  styleUrls: ['./products.css']
})
export class ProductsComponent implements OnInit {
  private readonly api = inject(ProductService);
  readonly cart = inject(CartService);
  readonly products = signal<Product[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly brands = signal<string[]>([]);
  readonly categories = signal<string[]>([]);

  search = '';
  brand = 'Todas';
  category = 'Todas';
  maxPrice = 300;

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true); this.error.set('');
    this.api.getAll({ search: this.search, brand: this.brand, category: this.category, maxPrice: this.maxPrice }).subscribe({
      next: data => {
        this.products.set(data);
        this.brands.set([...new Set(data.map(x => x.brand))].sort());
        this.categories.set([...new Set(data.map(x => x.category))].sort());
        this.loading.set(false);
      },
      error: err => { this.error.set(err.userMessage || 'Error al consultar /api/products'); this.loading.set(false); }
    });
  }

  add(product: Product): void {
    if (!product.id || product.stock <= 0) return;
    this.cart.add(product);
    this.message.set(`${product.name} agregado al carrito.`);
    setTimeout(() => this.message.set(''), 1800);
  }

  reset(): void { this.search=''; this.brand='Todas'; this.category='Todas'; this.maxPrice=300; this.load(); }
}
