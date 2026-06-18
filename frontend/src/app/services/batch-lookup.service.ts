import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { APP_CONFIG } from '../config/config';

@Injectable({
  providedIn: 'root'
})
export class BatchLookupService {
  private baseUrl = `${APP_CONFIG.BASE_URL}/api/production/batch`;
  private traceabilityUrl = APP_CONFIG.API.BATCH_TRACEABILITY;

  constructor(private http: HttpClient) { }

  /**
   * Fetch merged batch details (from production, casting, etc.) by batch number.
   * Useful for auto-populating fields in new forms.
   */
  getBatchDetails(batchNo: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/${batchNo}`);
  }

  /**
   * Read the normalized shared batch fields from the lookup payload.
   */
  getSharedFields(batchNo: string): Observable<any> {
    return this.getBatchDetails(batchNo);
  }

  /**
   * Fetch full traceability history for a batch.
   */
  getBatchTraceability(batchNo: string): Observable<any> {
    return this.http.get(`${this.traceabilityUrl}/${batchNo}`);
  }
}
