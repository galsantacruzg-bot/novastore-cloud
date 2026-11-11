import { Routes } from '@angular/router';
import { HomeComponent } from './features/storefront/home';
import { ProductsComponent } from './features/storefront/products';
import { AdminDashboardComponent } from './features/management/admin-dashboard';
import { ProductAdminComponent } from './features/management/product-admin';
import { CustomerAdminComponent } from './features/management/customer-admin';
import { OrdersComponent } from './features/management/orders';
import { IntegrationComponent } from './features/diagnostics/integration';
import { AccountComponent } from './features/accounts/account';
import { CartComponent } from './features/storefront/cart';
import { adminGuard } from './core/guards/admin.guard';
import { sessionGuard } from './core/guards/session.guard';

export const routes: Routes = [
  { path: 'acceso', component: AccountComponent, title: 'NovaStore | Acceder o crear cuenta' },
  { path: 'cuenta', component: AccountComponent, title: 'NovaStore | Mi cuenta' },
  { path: 'admin/login', redirectTo: 'acceso', pathMatch: 'full' },
  { path: '', component: HomeComponent, canActivate: [sessionGuard], title: 'NovaStore | Inicio' },
  { path: 'productos', component: ProductsComponent, canActivate: [sessionGuard], title: 'NovaStore | Productos' },
  { path: 'carrito', component: CartComponent, canActivate: [sessionGuard], title: 'NovaStore | Carrito' },
  { path: 'gestion', component: AdminDashboardComponent, canActivate: [adminGuard], title: 'NovaStore | Administración' },
  { path: 'gestion/productos', component: ProductAdminComponent, canActivate: [adminGuard], title: 'NovaStore | CRUD Productos' },
  { path: 'gestion/clientes', component: CustomerAdminComponent, canActivate: [adminGuard], title: 'NovaStore | Clientes' },
  { path: 'gestion/pedidos', component: OrdersComponent, canActivate: [adminGuard], title: 'NovaStore | Pedidos' },
  { path: 'integracion', component: IntegrationComponent, canActivate: [sessionGuard], title: 'NovaStore | Integración API' },
  { path: '**', redirectTo: '' }
];
