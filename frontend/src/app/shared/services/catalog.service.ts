import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CatalogService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.bffUrl}/bff/catalog`;

  getTariffs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/tariffs`);
  }

  getAddons(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/addons`);
  }
}
