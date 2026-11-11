import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CartService } from '../../core/services/cart.service';
import { OrderService } from '../../core/services/order.service';

@Component({
  selector: 'app-cart',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './cart.html',
  styleUrls: ['./cart.css']
})
export class CartComponent {
  readonly cart = inject(CartService);
  readonly auth = inject(AuthService);
  private readonly orders = inject(OrderService);
  private readonly fb = inject(FormBuilder);
  readonly message = signal('');
  readonly error = signal('');
  readonly busy = signal(false);

  readonly checkoutForm = this.fb.nonNullable.group({
    name: [this.auth.session()?.username || '', [Validators.required, Validators.minLength(3)]],
    email: [this.auth.session()?.email || '', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.minLength(7)]],
    shippingAddress: ['', [Validators.required, Validators.minLength(8)]]
  });

  checkout(): void {
    if (!this.cart.items().length) { this.error.set('Tu carrito está vacío.'); return; }
    if (this.checkoutForm.invalid) { this.checkoutForm.markAllAsTouched(); this.error.set('Completa los datos de entrega.'); return; }
    this.busy.set(true); this.error.set(''); this.message.set('');
    this.orders.guestCheckout({
      ...this.checkoutForm.getRawValue(),
      items: this.cart.items().map(line => ({ productId: line.product.id!, quantity: line.quantity }))
    }).subscribe({
      next: order => {
        this.busy.set(false);
        this.message.set(`Pedido #${order.id} registrado correctamente por ${order.total.toFixed(2)} USD.`);
        this.cart.clear();
      },
      error: err => { this.busy.set(false); this.error.set(err.userMessage || 'No fue posible registrar la compra.'); }
    });
  }
}
