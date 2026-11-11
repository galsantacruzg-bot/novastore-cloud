import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Product } from '../../core/models/product.model';
import { ProductService } from '../../core/services/product.service';

@Component({
  selector: 'app-product-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './product-admin.html',
  styleUrls: ['./product-admin.css']
})
export class ProductAdminComponent implements OnInit {
  private readonly api = inject(ProductService);
  private readonly fb = inject(FormBuilder);

  readonly products = signal<Product[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly message = signal('');
  readonly error = signal('');
  readonly busy = signal(false);

  readonly form = this.fb.nonNullable.group({
    code: ['', [Validators.required, Validators.maxLength(20)]],
    name: ['', [Validators.required, Validators.minLength(3)]],
    brand: ['', Validators.required],
    category: ['', Validators.required],
    price: [0, [Validators.required, Validators.min(0.01)]],
    stock: [0, [Validators.required, Validators.min(0)]],
    rating: [4.5, [Validators.required, Validators.min(0), Validators.max(5)]],
    image: ['assets/products/headphones.svg', Validators.required],
    description: ['', [Validators.required, Validators.minLength(10)]],
    active: [true]
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.getAll().subscribe({
      next: data => this.products.set(data),
      error: err => this.error.set(err.userMessage || 'Error al consultar productos.')
    });
  }

  edit(product: Product): void {
    this.selectedId.set(product.id ?? null);
    this.form.patchValue(product);
    this.message.set(`Editando ID #${product.id}`);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancel(): void {
    this.selectedId.set(null);
    this.form.reset({
      code: '', name: '', brand: '', category: '', price: 0, stock: 0, rating: 4.5,
      image: 'assets/products/headphones.svg', description: '', active: true
    });
    this.message.set('');
    this.error.set('');
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Revisa los campos obligatorios del formulario.');
      return;
    }

    this.busy.set(true);
    this.error.set('');
    const product: Product = this.form.getRawValue();
    const id = this.selectedId();
    const request = id ? this.api.update(id, product) : this.api.create(product);

    request.subscribe({
      next: saved => {
        this.message.set(id ? `Producto #${saved.id} actualizado con PUT.` : `Producto #${saved.id} registrado con POST.`);
        this.busy.set(false);
        this.selectedId.set(null);
        this.form.reset({
          code: '', name: '', brand: '', category: '', price: 0, stock: 0, rating: 4.5,
          image: 'assets/products/headphones.svg', description: '', active: true
        });
        this.load();
      },
      error: err => {
        this.error.set(err.userMessage || 'La API rechazó la operación.');
        this.busy.set(false);
      }
    });
  }

  remove(product: Product): void {
    if (!product.id || !confirm(`¿Eliminar ${product.name}? Esta acción ejecuta DELETE /api/products/${product.id}`)) return;
    this.api.delete(product.id).subscribe({
      next: () => {
        this.message.set(`Producto #${product.id} eliminado con DELETE.`);
        this.load();
      },
      error: err => this.error.set(err.userMessage || 'No fue posible eliminar.')
    });
  }
}
