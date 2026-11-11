import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ProductService } from '../../core/services/product.service';
import { CustomerService } from '../../core/services/customer.service';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css']
})
export class AdminDashboardComponent implements OnInit {
  private readonly productsApi = inject(ProductService);
  private readonly customersApi = inject(CustomerService);
  private readonly ordersApi = inject(OrderService);

  readonly stats = signal({ products: 0, customers: 0, orders: 0, stock: 0 });
  readonly loading = signal(true);
  readonly error = signal('');

  ngOnInit(): void {
    forkJoin({
      products: this.productsApi.getAll(),
      customers: this.customersApi.getAll(),
      orders: this.ordersApi.getAll()
    }).subscribe({
      next: data => {
        this.stats.set({
          products: data.products.length,
          customers: data.customers.length,
          orders: data.orders.length,
          stock: data.products.reduce((sum, p) => sum + p.stock, 0)
        });
        this.loading.set(false);
      },
      error: err => {
        this.error.set(err.userMessage || 'No se pudo consultar el panel.');
        this.loading.set(false);
      }
    });
  }
}
