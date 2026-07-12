import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.bffUrl}/bff/dashboard`;

  getDashboardData(subscriptionId?: string): Observable<any> {
    let params = new HttpParams();
    if (subscriptionId) {
      params = params.set('subscriptionId', subscriptionId);
    }
    return this.http.get<any>(this.apiUrl, { params });
  }
}
