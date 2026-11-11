import { computed, Injectable, signal } from '@angular/core';
import { Product } from '../models/product.model';

export interface CartLine {
  product: Product;
  quantity: number;
}

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly key = 'novastore_cart';
  readonly items = signal<CartLine[]>(this.read());
  readonly count = computed(() => this.items().reduce((sum, line) => sum + line.quantity, 0));
  readonly total = computed(() => this.items().reduce((sum, line) => sum + line.product.price * line.quantity, 0));

  add(product: Product): void {
    const next = [...this.items()];
    const line = next.find(x => x.product.id === product.id);
    if (line) line.quantity = Math.min(line.quantity + 1, product.stock);
    else next.push({ product, quantity: 1 });
    this.commit(next);
  }

  change(productId: number, quantity: number): void {
    const next = this.items().map(line => line.product.id === productId
      ? { ...line, quantity: Math.max(1, Math.min(quantity, line.product.stock)) }
      : line);
    this.commit(next);
  }

  remove(productId: number): void { this.commit(this.items().filter(line => line.product.id !== productId)); }
  clear(): void { this.commit([]); }

  private commit(lines: CartLine[]): void {
    this.items.set(lines);
    localStorage.setItem(this.key, JSON.stringify(lines));
  }

  private read(): CartLine[] {
    try { return JSON.parse(localStorage.getItem(this.key) || '[]') as CartLine[]; }
    catch { return []; }
  }
}
