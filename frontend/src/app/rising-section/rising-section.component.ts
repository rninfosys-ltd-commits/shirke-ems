import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RisingSectionService } from '../services/RisingSectionService';
import { ProductionService } from '../services/ProductionService';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';

@Component({
  selector: 'app-rising-section',
  templateUrl: './rising-section.component.html',
  styleUrls: ['./rising-section.component.css']
})
export class RisingSectionComponent implements OnInit {

  showForm = false;
  form!: FormGroup;
  list: any[] = [];
  productionList: any[] = [];
  editId: number | null = null;

  shifts: string[] = [
    'Night (00:00 - 08:00)',
    'Morning (08:00 - 16:00)',
    'Afternoon (16:00 - 00:00)'
  ];

  // Filters
  filterFromDate: string = '';
  filterToDate: string = '';
  filterPlant: string = 'Plant 1';

  // Pagination
  currentPage = 1;
  pageSize = 10;
  totalPages = 0;
  pagedList: any[] = [];

  constructor(
    private fb: FormBuilder,
    private service: RisingSectionService,
    private productionService: ProductionService,
    private auth: AuthService,
    private router: Router,
    private workflowService: WorkflowService,
    private filterService: FilterService,
    private horizontalReportService: HorizontalReportService
  ) { }

  ngOnInit(): void {
    this.initForm();
    this.setShiftByTime();
    this.loadList();
    this.loadProductions();

    this.filterService.fromDate$.subscribe(d => {
      this.filterFromDate = d;
      this.loadList();
    });
    this.filterService.toDate$.subscribe(d => {
      this.filterToDate = d;
      this.loadList();
    });
  }

  setShiftByTime() {
    const hour = new Date().getHours();
    if (hour >= 0 && hour < 8) this.form.patchValue({ shift: this.shifts[0] });
    else if (hour >= 8 && hour < 16) this.form.patchValue({ shift: this.shifts[1] });
    else this.form.patchValue({ shift: this.shifts[2] });
  }

  initForm(): void {
    this.form = this.fb.group({
      plantNo: ['', Validators.required],
      batchNo: ['', Validators.required],
      risingTime: [this.getCurrentTime()],
      risingTempC: [0],
      moldPenetration: [0],
      ballTest: [''],
      remark: [''],
      shift: ['1', Validators.required],
      plantName: ['Plant 1']
    });
  }

  getCurrentTime(): string {
    const now = new Date();
    return now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  loadList(): void {
    const start = this.filterFromDate ? new Date(this.filterFromDate) : null;
    const end = this.filterToDate ? new Date(this.filterToDate) : null;

    this.service.getAll().subscribe(res => {
      let filtered = res || [];

      if (start) {
        filtered = filtered.filter((r: any) => new Date(r.createdDate) >= start);
      }
      if (end) {
        const endDay = new Date(end);
        endDay.setHours(23, 59, 59, 999);
        filtered = filtered.filter((r: any) => new Date(r.createdDate) <= endDay);
      }
      if (this.filterPlant) {
        filtered = filtered.filter((r: any) => r.plantName === this.filterPlant);
      }

      this.list = filtered;
      this.updatePagination();
    });
  }

  onFilterChange(): void {
    this.currentPage = 1;
    this.loadList();
  }

  onDateChange(): void {
    this.onFilterChange();
  }

  clearFilters(): void {
    this.filterFromDate = '';
    this.filterToDate = '';
    this.filterPlant = 'Plant 1';
    this.onFilterChange();
  }

  loadProductions(): void {
    this.productionService.getAll().subscribe(res => {
      this.productionList = res || [];
    });
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.list.length / this.pageSize);
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedList = this.list.slice(start, start + this.pageSize);
  }

  goToPage(p: number): void {
    this.currentPage = p;
    this.updatePagination();
  }

  openForm(): void {
    this.showForm = true;
    this.editId = null;
    this.form.reset({
      risingTime: this.getCurrentTime(),
      risingTempC: 0,
      moldPenetration: 0,
      plantName: 'Plant 1'
    });
    this.setShiftByTime();
  }

  edit(row: any): void {
    this.showForm = true;
    this.editId = row.id;
    this.form.patchValue(row);
  }

  delete(id: number): void {
    if (confirm('Are you sure you want to delete this record?')) {
      this.service.delete(id).subscribe(() => this.loadList());
    }
  }

  submit(): void {
    if (this.form.invalid) return;

    const payload = {
      ...this.form.value,
      userId: this.auth.getLoggedInUserId(),
      branchId: 1,
      orgId: 1
    };

    const req$ = this.editId 
      ? this.service.update(this.editId, payload)
      : this.service.save(payload);

    req$.subscribe(() => {
      this.showForm = false;
      this.loadList();
    });
  }

  cancel(): void {
    this.showForm = false;
    this.editId = null;
  }

  goToDashboard(): void {
    this.router.navigate(['/production-dashboard']);
  }

  onExportChange(event: any) {
    const value = event.target.value;
    if (value === 'excel') this.exportExcel();
    if (value === 'pdf') this.exportPdf();
    if (value === 'horizontal') this.exportHorizontalReport();
    event.target.value = '';
  }

  onImportSelect(event: any) {
    if (event.target.value === 'excel') {
      const fileInput = document.getElementById('risingExcelInput') as HTMLInputElement;
      fileInput?.click();
    }
    event.target.value = '';
  }

  onExcelSelect(event: any) {
    alert('Import coming later');
  }

  exportExcel() {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }
    this.workflowService.exportReport('RISING', this.filterFromDate, this.filterToDate, 'excel').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Rising_Report_${this.filterFromDate}_to_${this.filterToDate}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export Excel')
    });
  }

  exportPdf() {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }
    this.workflowService.exportReport('RISING', this.filterFromDate, this.filterToDate, 'pdf').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Rising_Report_${this.filterFromDate}_to_${this.filterToDate}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export PDF')
    });
  }

  exportHorizontalReport() {
    if (!this.filterFromDate || !this.filterToDate) { alert('Please select a date range first'); return; }
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'RISING').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `horizontal_report_${this.filterFromDate}_to_${this.filterToDate}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export horizontal report.')
    });
  }
}
