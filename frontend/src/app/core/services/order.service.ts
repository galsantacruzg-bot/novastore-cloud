import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CreateOrderRequest, GuestOrderRequest, Order } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/orders';
  getAll(): Observable<Order[]> { return this.http.get<Order[]>(this.baseUrl); }
  create(request: CreateOrderRequest): Observable<Order> { return this.http.post<Order>(this.baseUrl, request); }
  guestCheckout(request: GuestOrderRequest): Observable<Order> { return this.http.post<Order>(`${this.baseUrl}/guest`, request); }
}
