import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Customer } from '../../core/models/customer.model';
import { CustomerService } from '../../core/services/customer.service';

@Component({
  selector: 'app-customer-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './customer-admin.html',
  styleUrls: ['./customer-admin.css']
})
export class CustomerAdminComponent implements OnInit {
  private readonly api = inject(CustomerService);
  private readonly fb = inject(FormBuilder);

  readonly customers = signal<Customer[]>([]);
  readonly selectedId = signal<number | null>(null);
  readonly message = signal('');
  readonly error = signal('');

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['', [Validators.required, Validators.minLength(7)]]
  });

  ngOnInit(): void { this.load(); }

  load(): void {
    this.api.getAll().subscribe({
      next: data => this.customers.set(data),
      error: err => this.error.set(err.userMessage || 'Error al consultar clientes.')
    });
  }

  edit(customer: Customer): void {
    this.selectedId.set(customer.id ?? null);
    this.form.patchValue(customer);
  }

  cancel(): void {
    this.selectedId.set(null);
    this.form.reset({ name: '', email: '', phone: '' });
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.error.set('Completa correctamente nombre, correo y teléfono.');
      return;
    }
    const customer = this.form.getRawValue();
    const id = this.selectedId();
    const request = id ? this.api.update(id, customer) : this.api.create(customer);
    request.subscribe({
      next: saved => {
        this.message.set(id ? `Cliente #${saved.id} actualizado.` : `Cliente #${saved.id} registrado.`);
        this.cancel(); this.load();
      },
      error: err => this.error.set(err.userMessage || 'No fue posible guardar.')
    });
  }

  remove(customer: Customer): void {
    if (!customer.id || !confirm(`¿Eliminar a ${customer.name}?`)) return;
    this.api.delete(customer.id).subscribe({
      next: () => { this.message.set(`Cliente #${customer.id} eliminado.`); this.load(); },
      error: err => this.error.set(err.userMessage || 'No fue posible eliminar.')
    });
  }
}
