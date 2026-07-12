import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.bffUrl}/bff`;

  createOrder(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/orders`, data);
  }

  processPayment(data: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/payments`, data);
  }
}
