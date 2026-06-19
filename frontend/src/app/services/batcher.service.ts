import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/config';

@Injectable({
  providedIn: 'root'
})
export class BatcherService {
  private baseUrl = `${APP_CONFIG.BASE_URL}${APP_CONFIG.API.BATCHERS}`;

  constructor(private http: HttpClient) {}

  getAllBatchers(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getBatcherById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  createBatcher(batcher: any): Observable<any> {
    return this.http.post<any>(this.baseUrl, batcher);
  }

  updateBatcher(id: number, batcher: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}`, batcher);
  }

  deleteBatcher(id: number): Observable<any> {
    return this.http.delete<any>(`${this.baseUrl}/${id}`, { responseType: 'text' as 'json' });
  }
}
