import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.bffUrl}/bff/tickets`;

  getTickets(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  getTicketDetail(id: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  createTicket(data: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, data);
  }

  addComment(id: string, body: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${id}/comments`, { body });
  }
}
