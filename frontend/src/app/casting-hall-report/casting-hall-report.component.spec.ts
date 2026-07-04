import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { CastingHallReportComponent } from './casting-hall-report.component';
import { CastingHallReportService } from '../services/CastingHallReportService';
import { ProductionService } from '../services/ProductionService';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';
import { BatchLookupService } from '../services/batch-lookup.service';

describe('CastingHallReportComponent', () => {
  let component: CastingHallReportComponent;
  let fixture: ComponentFixture<CastingHallReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CastingHallReportComponent],
      imports: [ReactiveFormsModule, FormsModule, RouterTestingModule],
      providers: [
        { provide: CastingHallReportService, useValue: { getAll: () => of({ content: [], totalElements: 0, totalPages: 0 }), save: () => of({}), update: () => of({}), delete: () => of({}) } },
        { provide: ProductionService, useValue: { getAll: () => of([]) } },
        { provide: AuthService, useValue: { getLoggedInUserId: () => 1 } },
        { provide: Router, useValue: { navigate: jasmine.createSpy('navigate') } },
        { provide: WorkflowService, useValue: { exportReport: () => of(new Blob()) } },
        { provide: FilterService, useValue: { fromDate$: of(''), toDate$: of(''), setFromDate: () => {}, setToDate: () => {} } },
        { provide: HorizontalReportService, useValue: {} },
        { provide: BatchLookupService, useValue: { getBatchDetails: () => of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CastingHallReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default blank remarks to OK', () => {
    expect(component.normalizeRemarkValue('')).toBe('OK');
  });

  it('should treat custom remarks as a custom selection', () => {
    expect(component.getRemarkSelectionValue('Needs review')).toBe('custom');
  });
});
