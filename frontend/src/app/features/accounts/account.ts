import { CommonModule } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './account.html',
  styleUrls: ['./account.css']
})
export class AccountComponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly mode = signal<'login'|'register'|'verify'>('login');
  readonly message = signal('');
  readonly error = signal('');
  readonly busy = signal(false);
  readonly session = this.auth.session;

  readonly loginForm = this.fb.nonNullable.group({
    identifier: ['', Validators.required],
    password: ['', Validators.required]
  });

  readonly registerForm = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(40)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(100)]],
    confirmPassword: ['', Validators.required]
  });

  readonly verifyForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
  });

  setMode(mode: 'login'|'register'|'verify'): void {
    this.mode.set(mode);
    this.error.set(''); this.message.set('');
    if (mode === 'verify') {
      const email = this.registerForm.controls.email.value || this.loginForm.controls.identifier.value;
      if (email.includes('@') && !this.verifyForm.controls.email.value) this.verifyForm.controls.email.setValue(email);
    }
  }

  login(): void {
    if (this.loginForm.invalid) return this.loginForm.markAllAsTouched();
    this.busy.set(true); this.error.set(''); this.message.set('');
    const v = this.loginForm.getRawValue();
    this.auth.login(v.identifier.trim(), v.password).subscribe({
      next: session => {
        this.busy.set(false);
        const requested = this.route.snapshot.queryParamMap.get('returnUrl');
        const safeReturn = requested && requested.startsWith('/') && !requested.startsWith('//')
          && requested !== '/acceso' && !requested.startsWith('/admin/login') ? requested : null;
        const home = session.role === 'ADMIN' ? '/gestion' : '/';
        const target = safeReturn?.startsWith('/gestion') && session.role !== 'ADMIN' ? '/' : safeReturn || home;
        void this.router.navigateByUrl(target);
      },
      error: err => { this.busy.set(false); this.error.set(err.userMessage || 'No fue posible iniciar sesión.'); }
    });
  }

  register(): void {
    if (this.registerForm.invalid) return this.registerForm.markAllAsTouched();
    const v = this.registerForm.getRawValue();
    if (v.password !== v.confirmPassword) {
      this.error.set('Las contraseñas no coinciden.'); return;
    }
    this.busy.set(true); this.error.set(''); this.message.set('');
    const email = v.email.trim().toLowerCase();
    this.auth.register({ username: v.username.trim(), email, password: v.password }).subscribe({
      next: result => {
        this.busy.set(false);
        this.verifyForm.controls.email.setValue(email);
        this.verifyForm.controls.code.reset();
        this.message.set(result.message);
        if (result.verificationRequired) {
          this.mode.set('verify');
        } else {
          this.loginForm.controls.identifier.setValue(email);
          this.mode.set('login');
        }
      },
      error: err => { this.busy.set(false); this.error.set(err.userMessage || 'No fue posible crear la cuenta.'); }
    });
  }

  verify(): void {
    if (this.verifyForm.invalid) return this.verifyForm.markAllAsTouched();
    const v = this.verifyForm.getRawValue();
    this.busy.set(true); this.error.set(''); this.message.set('');
    this.auth.verify(v.email.trim().toLowerCase(), v.code).subscribe({
      next: result => {
        this.busy.set(false); this.message.set(result.message + ' Ya puedes iniciar sesión.');
        this.loginForm.controls.identifier.setValue(v.email.trim());
        this.mode.set('login');
      },
      error: err => { this.busy.set(false); this.error.set(err.userMessage || 'No se pudo verificar la cuenta.'); }
    });
  }

  resend(): void {
    if (this.verifyForm.controls.email.invalid || this.busy()) {
      this.verifyForm.controls.email.markAsTouched(); return;
    }
    this.busy.set(true); this.error.set(''); this.message.set('');
    this.auth.resend(this.verifyForm.controls.email.value.trim().toLowerCase()).subscribe({
      next: result => {this.busy.set(false); this.message.set(result.message);},
      error: err => {this.busy.set(false); this.error.set(err.userMessage || 'No se pudo reenviar el código.');}
    });
  }

  logout(): void {
    this.auth.logout(); this.mode.set('login'); this.message.set('Sesión cerrada.');
    void this.router.navigateByUrl('/acceso');
  }
}
