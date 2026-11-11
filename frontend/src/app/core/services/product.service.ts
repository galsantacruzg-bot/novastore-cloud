import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../models/product.model';

@Injectable({ providedIn: 'root' })
export class ProductService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/products';

  getAll(filters?: { search?: string; brand?: string; category?: string; maxPrice?: number }): Observable<Product[]> {
    let params = new HttpParams();
    if (filters?.search) params = params.set('search', filters.search);
    if (filters?.brand && filters.brand !== 'Todas') params = params.set('brand', filters.brand);
    if (filters?.category && filters.category !== 'Todas') params = params.set('category', filters.category);
    if (filters?.maxPrice) params = params.set('maxPrice', filters.maxPrice.toString());
    return this.http.get<Product[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Product> {
    return this.http.get<Product>(`${this.baseUrl}/${id}`);
  }

  create(product: Product): Observable<Product> {
    return this.http.post<Product>(this.baseUrl, product);
  }

  update(id: number, product: Product): Observable<Product> {
    return this.http.put<Product>(`${this.baseUrl}/${id}`, product);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  categories(): Observable<string[]> {
    return this.http.get<string[]>('/api/categories');
  }
}
