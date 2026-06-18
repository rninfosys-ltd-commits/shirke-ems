import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RisingSectionService } from '../services/RisingSectionService';
import { ProductionService } from '../services/ProductionService';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';
import { BatchLookupService } from '../services/batch-lookup.service';
import * as XLSX from 'xlsx';

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
    'Night (00:00 - 08:00) [1st Shift]',
    'Morning (08:00 - 16:00) [2nd Shift]',
    'Afternoon (16:00 - 00:00) [3rd Shift]'
  ];

  // Filters
  filterFromDate: string = '';
  filterToDate: string = '';
  filterPlant: string = 'Plant 1';
  filterShift: string = '';

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
    private horizontalReportService: HorizontalReportService,
    private batchLookup: BatchLookupService
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

  risingRemarkOptions: { id: number, english: string, marathi: string, value: string }[] = [
    { id: 1, english: 'Main mixer discharge valve not opened.', marathi: 'मुख्य मिक्सर डिस्चार्ज व्हॉल्व्ह उघडलेला नाही.', value: '1' },
    { id: 2, english: 'Aluminium mixer valve not opened.', marathi: 'अॅल्युमिनियम मिक्सर व्हॉल्व्ह उघडलेला नाही.', value: '2' },
    { id: 3, english: 'Double aluminium powder added.', marathi: 'दुहेरी प्रमाणात अॅल्युमिनियम पावडर टाकण्यात आली.', value: '3' },
    { id: 4, english: 'Soluble oil not added.', marathi: 'सोल्युबल ऑइल टाकलेले नाही.', value: '4' },
    { id: 5, english: 'Ferry carriage stuck.', marathi: 'फेरी कॅरेज अडकले आहे.', value: '5' },
    { id: 6, english: 'Mixer agitator jammed.', marathi: 'मिक्सर अॅजिटेटर जॅम झाला आहे.', value: '6' },
    { id: 7, english: 'Cement and lime powder screw jammed.', marathi: 'सिमेंट आणि लाईम पावडर स्क्रू जॅम झाला आहे.', value: '7' },
    { id: 8, english: 'Temperature increased due to ferry carriage under maintenance.', marathi: 'फेरी कॅरेज देखभालीखाली असल्यामुळे तापमान वाढले.', value: '8' },
    { id: 9, english: 'Mould derailed on ferry carriage.', marathi: 'फेरी कॅरेजवर साचा (मोल्ड) रुळावरून घसरला.', value: '9' }
  ];

  initForm(): void {
    const now = this.getCurrentTime();
    this.form = this.fb.group({
      plantNo: [''],
      batchNo: ['', Validators.required],
      risingStartTime: [now],
      dischargeTime: [''],
      mouldNo: [''],
      mouldHeight: [0],
      mouldFlow: [0],

      // Keep `remark` for backward compatibility (backend expects string),
      // but UI will store multi-select selections in `remarks` (string array).
      remark: [''],
      remarks: [[] as string[]],

      shift: ['1', Validators.required],
      plantName: ['Plant 1']
    });

    // Auto-calculate total rising time whenever either time changes
    this.form.get('risingStartTime')?.valueChanges.subscribe(() => this.calcTotalRisingTime());
    this.form.get('dischargeTime')?.valueChanges.subscribe(() => this.calcTotalRisingTime());
  }

  private buildRemarkTextFromIds(ids: string[] | null | undefined): string {
    const safe = (ids || []).filter(Boolean);
    const selected = this.risingRemarkOptions.filter(o => safe.includes(o.value));
    if (!selected.length) return '';

    // Use bilingual text per remark; join multiple selections with '; '
    return selected.map(o => `${o.english} / ${o.marathi}`).join('; ');
  }

  private parseRemarkTextToIds(text: string | null | undefined): string[] {
    if (!text) return [];
    const trimmed = String(text).trim();
    if (!trimmed) return [];

    // If backend stored our `value` ids, handle that format first
    const idMatches = trimmed.match(/\b[1-9]\b/g);
    if (idMatches && idMatches.length) return Array.from(new Set(idMatches));

    // Otherwise attempt to match by bilingual string presence
    const ids: string[] = [];
    for (const opt of this.risingRemarkOptions) {
      const key = `${opt.english} / ${opt.marathi}`;
      if (trimmed.includes(key)) ids.push(opt.value);
      else if (trimmed.includes(opt.english) || trimmed.includes(opt.marathi)) ids.push(opt.value);
    }
    return Array.from(new Set(ids));
  }

  toggleRemark(value: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;
    const current: string[] = this.form.get('remarks')?.value ?? [];

    const set = new Set<string>(current);
    if (checked) set.add(value);
    else set.delete(value);

    const next = Array.from(set);
    this.form.patchValue({
      remarks: next,
      // keep backward compatible string field in sync
      remark: this.buildRemarkTextFromIds(next)
    });
  }

  getCurrentTime(): string {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    return `${hh}:${mm}`;
  }

  // ===== TOTAL RISING TIME CALCULATION =====
  totalRisingTimeDisplay = '';

  calcTotalRisingTime(): void {
    const start = this.form.get('risingStartTime')?.value;
    const end = this.form.get('dischargeTime')?.value;
    if (!start || !end) { this.totalRisingTimeDisplay = ''; return; }
    try {
      const [sh, sm] = start.split(':').map(Number);
      const [eh, em] = end.split(':').map(Number);
      let diff = (eh * 60 + em) - (sh * 60 + sm);
      if (diff < 0) diff += 24 * 60;
      const h = Math.floor(diff / 60);
      const m = diff % 60;
      this.totalRisingTimeDisplay = `${h} Hours ${m} Minutes`;
    } catch { this.totalRisingTimeDisplay = ''; }
  }

  useCurrentTimeForRisingStart(): void {
    this.form.patchValue({ risingStartTime: this.getCurrentTime() });
  }

  useCurrentTimeForDischarge(): void {
    this.form.patchValue({ dischargeTime: this.getCurrentTime() });
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
      if (this.filterShift) {
        filtered = filtered.filter((r: any) => (r.shift || '').toLowerCase().includes(this.filterShift.toLowerCase()));
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
    this.filterShift = '';
    this.onFilterChange();
  }

  loadProductions(): void {
    this.productionService.getAll().subscribe(res => {
      this.productionList = res || [];
    });
  }

  onBatchChange(event?: any): void {
    const batchNo = this.form.get('batchNo')?.value;
    if (!batchNo) return;
    this.batchLookup.getBatchDetails(batchNo).subscribe({
      next: (res) => {
        const shared = res?.sharedFields || {};
        if (res?.casting || res?.production) {
          this.form.patchValue({
            shift: shared.shift || res?.casting?.shift || res?.production?.shift || this.form.value.shift,
            plantName: shared.plantName || res?.casting?.plantName || res?.production?.plantName || this.form.value.plantName,
            mouldNo: shared.mouldNo ?? res?.casting?.mouldNo ?? this.form.value.mouldNo,
            mouldHeight: shared.mouldHeight ?? res?.casting?.mouldHeight ?? res?.casting?.height ?? this.form.value.mouldHeight,
            mouldFlow: shared.mouldFlow ?? res?.casting?.mouldFlow ?? this.form.value.mouldFlow
          });
        }
      },
      error: (err) => console.log('Batch lookup error:', err)
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
    const now = this.getCurrentTime();
    this.showForm = true;
    this.editId = null;
    this.totalRisingTimeDisplay = '';
    this.form.reset({
      risingStartTime: now,
      dischargeTime: '',
      mouldNo: '',
      mouldHeight: 0,
      mouldFlow: 0,
      plantName: 'Plant 1',
      remark: '',
      remarks: []
    });
    this.setShiftByTime();
  }

  edit(row: any): void {
    this.showForm = true;
    this.editId = row.id;

    // Pre-fill multi-select using backend stored remark text
    const backendText = row?.remarks ?? row?.remark ?? '';
    const selectedIds = this.parseRemarkTextToIds(backendText);

    this.form.patchValue({
      ...row,
      remarks: selectedIds,
      // keep remark updated too for backward compat/export
      remark: this.buildRemarkTextFromIds(selectedIds)
    });
  }

  delete(id: number): void {
    if (confirm('Are you sure you want to delete this record?')) {
      this.service.delete(id).subscribe(() => this.loadList());
    }
  }

  submit(): void {
    if (this.form.invalid) return;

    const fv = this.form.value;
    const selectedIds: string[] = fv.remarks ?? [];
    const remarkText = this.buildRemarkTextFromIds(selectedIds);

    const payload = {
      ...fv,
      // backward compatible fields (backend DTO has both)
      remark: remarkText,
      remarks: remarkText,

      // send dischargeTime as risingEndTime for backend compat
      risingEndTime: fv.dischargeTime,
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
    const data = this.list.map(r => ({
      PlantNo: r.plantNo ?? '',
      BatchNo: r.batchNo ?? '',
      Shift: r.shift ?? '',
      RisingStartTime: r.risingStartTime ?? '',
      DischargeTime: r.dischargeTime ?? r.risingEndTime ?? '',
      TotalRisingTime: r.risingTime ?? '',
      MouldNo: r.mouldNo ?? '',
      MouldHeight: r.mouldHeight ?? '',
      MouldFlow: r.mouldFlow ?? '',
      RisingTemperature: r.risingTemperature ?? '',
      Remark: r.remark ?? '',
      PlantName: r.plantName ?? ''
    }));

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Rising');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Rising_Report_${this.filterFromDate || 'all'}_to_${this.filterToDate || 'all'}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
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

  downloadRising(r: any, format: string = 'pdf'): void {
    if (!r || !r.batchNo) {
      alert('No batch selected to download');
      return;
    }
    this.workflowService.downloadReport(r.batchNo, 'RISING', format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const ext = format === 'excel' ? 'xlsx' : 'pdf';
        a.download = `workflow_report_${r.batchNo}_RISING.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { alert('Failed to download report.'); }
    });
  }

  /** Download combined horizontal Excel for this rising batch */
  downloadHorizontalReport(r: any): void {
    const batchNo = r?.batchNo;
    if (!batchNo) {
      alert('No batch number available');
      return;
    }
    this.horizontalReportService.downloadLifecycleExcel(batchNo, 'RISING').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `horizontal_report_${batchNo}.xlsx`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to download horizontal report.')
    });
  }

  exportHorizontalReport() {
    if (!this.filterFromDate || !this.filterToDate) { alert('Please select a date range first'); return; }
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'RISING', this.filterPlant, this.filterShift).subscribe({
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
