import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { Customer } from '../../core/models/customer.model';
import { Order } from '../../core/models/order.model';
import { Product } from '../../core/models/product.model';
import { CustomerService } from '../../core/services/customer.service';
import { OrderService } from '../../core/services/order.service';
import { ProductService } from '../../core/services/product.service';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './orders.html',
  styleUrls: ['./orders.css']
})
export class OrdersComponent implements OnInit {
  private readonly ordersApi = inject(OrderService);
  private readonly productsApi = inject(ProductService);
  private readonly customersApi = inject(CustomerService);
  private readonly fb = inject(FormBuilder);

  readonly orders = signal<Order[]>([]);
  readonly products = signal<Product[]>([]);
  readonly customers = signal<Customer[]>([]);
  readonly message = signal('');
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    customerId: [0, [Validators.required, Validators.min(1)]],
    productId: [0, [Validators.required, Validators.min(1)]],
    quantity: [1, [Validators.required, Validators.min(1), Validators.max(20)]]
  });

  ngOnInit(): void { this.refreshAll(); }

  refreshAll(): void {
    forkJoin({
      orders: this.ordersApi.getAll(),
      products: this.productsApi.getAll(),
      customers: this.customersApi.getAll()
    }).subscribe({
      next: data => {
        this.orders.set(data.orders);
        this.products.set(data.products);
        this.customers.set(data.customers);
      },
      error: err => this.error.set(err.userMessage || 'No se pudieron consultar los recursos.')
    });
  }

  createOrder(): void {
    if (this.form.invalid) {
      this.error.set('Selecciona cliente, producto y cantidad.');
      return;
    }
    const value = this.form.getRawValue();
    this.ordersApi.create({
      customerId: value.customerId,
      items: [{ productId: value.productId, quantity: value.quantity }]
    }).subscribe({
      next: order => {
        this.message.set(`Pedido #${order.id} creado con POST. Total ${order.total.toFixed(2)} USD.`);
        this.form.patchValue({ productId: 0, quantity: 1 });
        this.refreshAll();
      },
      error: err => this.error.set(err.userMessage || 'No fue posible crear el pedido.')
    });
  }
}
