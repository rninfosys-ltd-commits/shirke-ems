import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { WireCuttingReportService } from '../services/WireCuttingReportService';
import { ProductionService } from '../services/ProductionService';
import * as bootstrap from 'bootstrap';
import { CastingHallReportService } from '../services/CastingHallReportService';
import { Router } from '@angular/router';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';
import { WireCuttingReport } from '../models/wire-cutting';
import { AuthService } from '../services/auth.service';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';
import { BatchLookupService } from '../services/batch-lookup.service';
@Component({
  selector: 'app-wire-cutting-report',
  templateUrl: './wire-cutting-report.component.html',
  styleUrls: ['./wire-cutting-report.component.css']
})
export class WireCuttingReportComponent implements OnInit {
  currentUserRole = '';

  showForm = false;
  form!: FormGroup;

  // list: any[] = [];
  // filteredList: any[] = [];
  productionList: any[] = [];

  editId: number | null = null;
  // selected: any = null;


  list: WireCuttingReport[] = [];
  filteredList: WireCuttingReport[] = [];
  // pagedList: WireCuttingReport[] = [];

  selected: WireCuttingReport | null = null;

  // importPreviewList: WireCuttingReport[] = [];
  // pagedImportPreview: WireCuttingReport[] = [];

  filterFromDate = '';
  filterToDate = '';
  filterPlant = 'Plant 1';
  filterShift = '';
  castingList: any[] = [];

  allProductionList: any[] = [];
  availableProductionList: any[] = [];
  // ================= IMPORT =================
  showImportModal = false;
  importColumns: string[] = [];
  importPreviewList: any[] = [];
  pagedImportPreview: any[] = [];

  importPageSize = 5;
  importCurrentPage = 1;
  importTotalPages = 0;

  // ================= PAGINATION =================
  pageSize = 5;
  currentPage = 1;
  totalPages = 0;
  pagedList: any[] = [];

  shifts: string[] = [
    'Night (00:00 - 08:00) [1st Shift]',
    'Morning (08:00 - 16:00) [2nd Shift]',
    'Afternoon (16:00 - 00:00) [3rd Shift]'
  ];


  constructor(
    private fb: FormBuilder,
    private service: WireCuttingReportService,
    private productionService: ProductionService,
    private castingService: CastingHallReportService,
    private auth: AuthService,
    private router: Router,
    private workflowService: WorkflowService,
    private filterService: FilterService,
    private horizontalReportService: HorizontalReportService,
    private batchLookup: BatchLookupService
  ) { }

  ngOnInit(): void {
    const today = new Date().toISOString().substring(0, 10);
    this.loadCurrentUserRole();

    this.form = this.fb.group({
      cuttingDate: [today, Validators.required],
      shift: ['', Validators.required],
      plantName: ['Plant 1', Validators.required],
      batchNo: ['', Validators.required],
      mouldNo: [0],
      size: [''],
      ballTestMm: [0],
      time: [''],
      cuttingStartTime: [''],
      totalItem: [0],
      cuttingTempC: [0],
      cuttingHours: [0],
      remark: ['']
    });

    this.form.valueChanges.subscribe(() => {
      this.calculateTotals();
    });

    this.setShiftByTime();
    this.load();
    this.loadProduction();
    this.loadCasting();

    this.filterService.fromDate$.subscribe(d => {
      this.filterFromDate = d;
      this.applyFilters();
    });
    this.filterService.toDate$.subscribe(d => {
      this.filterToDate = d;
      this.applyFilters();
    });
  }

  setShiftByTime() {
    const hour = new Date().getHours();
    if (hour >= 0 && hour < 8) this.form.patchValue({ shift: this.shifts[0] });
    else if (hour >= 8 && hour < 16) this.form.patchValue({ shift: this.shifts[1] });
    else this.form.patchValue({ shift: this.shifts[2] });
  }


  private buildMergedExportData() {
    return this.filteredList.map(cutting => {

      const production = this.allProductionList.find(
        p => p.batchNo === cutting.batchNo
      ) || {};

      const casting = this.castingList.find(
        c => c.batchNo === cutting.batchNo
      ) || {};

      return {
        ...production,
        ...casting,
        ...cutting
      };
    });
  }

  getCurrentTime(): string {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }

  useCurrentTimeForCuttingStart(): void {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    this.form.patchValue({ cuttingStartTime: `${hh}:${mm}` });
  }
  // ================= LOAD =================
  load(preservePage = false) {
    this.service.getAll().subscribe(res => {
      this.list = res || [];
      this.applyFilters(preservePage);
      this.filterAvailableBatches();   // ⭐ IMPORTANT
      this.updatePagination();
    });


  }
  loadCasting() {
    this.castingService.getAll(0, 1000).subscribe(res => {
      this.castingList = res?.content || [];
    });
  }
  goToDashboard() {
    this.router.navigate(['/production-dashboard']);
  }


  loadProduction() {
    this.productionService.getAll().subscribe(res => {
      this.allProductionList = res || [];
      this.filterAvailableBatches();   // ⭐ IMPORTANT
    });
  }


  filterAvailableBatches() {
    if (!this.allProductionList.length) return;

    const usedBatchNos = this.list.map(r => r.batchNo);
    const selectedPlant = this.form?.get('plantName')?.value;

    let available = this.allProductionList.filter(
      p => !usedBatchNos.includes(p.batchNo)
    );

    if (selectedPlant) {
      available = available.filter(p => p.plantName === selectedPlant);
    }

    this.availableProductionList = available;
    this.productionList = this.availableProductionList;
  }

  onPlantChange() {
    this.form.patchValue({ batchNo: '' });
    this.filterAvailableBatches();
  }

  onBatchChange(event?: any) {
    const batchNo = this.form.get('batchNo')?.value;
    if (!batchNo) return;
    this.batchLookup.getBatchDetails(batchNo).subscribe({
      next: (res) => {
        const shared = res?.sharedFields || {};
        if (res?.casting) {
          this.form.patchValue({
            mouldNo: shared.mouldNo ?? res.casting.mouldNo ?? 0,
            size: shared.size || this.form.value.size
          });
        }

        if (res?.production) {
          this.form.patchValue({
            shift: shared.shift || res.production.shift || this.form.value.shift,
            plantName: shared.plantName || res.production.plantName || this.form.value.plantName,
            cuttingDate: res.production.createdDate
              ? new Date(res.production.createdDate).toISOString().substring(0, 10)
              : new Date().toISOString().substring(0, 10)
          });
        }
      },
      error: (err) => console.log('Lookup error:', err)
    });
  }

  // ================= FILTER =================
  applyFilters(preservePage = false) {
    if (
      this.filterFromDate &&
      this.filterToDate &&
      new Date(this.filterToDate) < new Date(this.filterFromDate)
    ) {
      alert('To Date cannot be earlier than From Date');
      return;
    }

    const from = this.filterFromDate ? new Date(this.filterFromDate).getTime() : null;
    const to = this.filterToDate
      ? new Date(this.filterToDate + 'T23:59:59').getTime()
      : null;

    this.filteredList = this.list.filter(r => {
      // ✅ PLANT FILTER
      if (this.filterPlant && r.plantName !== this.filterPlant) return false;

      // ✅ SHIFT FILTER
      if (this.filterShift && r.shift !== this.filterShift) return false;

      if (!r.createdDate) return false;
      const d = new Date(r.createdDate).getTime();
      return (!from || d >= from) && (!to || d <= to);
    });
    if (!preservePage) {
      this.currentPage = 1;
    }
    this.updatePagination();
  }


  updatePagination() {
    this.totalPages = Math.ceil(this.filteredList.length / this.pageSize);
    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.pagedList = this.filteredList.slice(start, end);
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updatePagination();
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  onDateChange() {
    this.filterService.setFromDate(this.filterFromDate);
    this.filterService.setToDate(this.filterToDate);
  }

  clearFilters() {
    this.filterFromDate = '';
    this.filterToDate = '';
    this.filterPlant = 'Plant 1';
    this.filterShift = '';
    this.onDateChange();
  }

  openForm() {
    this.showForm = true;
    this.editId = null;

    this.form.reset({
      cuttingDate: new Date().toISOString().substring(0, 10),
      plantName: 'Plant 1',
      time: this.getCurrentTime()
    });

    this.productionList = [...this.availableProductionList];
    this.setShiftByTime();
  }


  edit(row: any) {
    this.editId = row.id;
    this.showForm = true;

    this.form.patchValue(row);
    this.sizeDetails = row.sizeDetails || [];

    // ✅ EDIT MODE → show ALL batches matching the plant
    const selectedPlant = row.plantName;
    this.productionList = this.allProductionList.filter(p => !selectedPlant || p.plantName === selectedPlant);
  }


  delete(id: number) {
    if (confirm('Delete this wire cutting entry?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }

  sizeDetails: any[] = [];

  onSizeChange() {
    const sizeStr = this.form.get('size')?.value;
    this.sizeDetails = [];
    if (!sizeStr) return;

    // Accept both "x" and "×" so older records still work.
    const parts = sizeStr.split(/[x×]/i).map((s: string) => s.trim());
    if (parts.length === 3) {
      const length = Number(parts[0]) || 0;
      const width = Number(parts[1]) || 0;
      const thicknesses = parts[2].split('/').map((s: string) => Number(s.trim()) || 0);

      for (const t of thicknesses) {
        this.sizeDetails.push({
          length: length,
          width: width,
          height: t,
          quantity: 0,
          quantityTotal: 0,
          breakage: 0,
          netQuantity: 0
        });
      }
    }
    this.calculateTotals();
  }

  calculateTotals() {
    let totalItem = 0;

    for (const item of this.sizeDetails) {
      const q = Number(item.quantity) || 0;
      const b = Number(item.breakage) || 0;

      // Net Quantity = Gross Quantity (No deduction for breakage)
      item.netQuantity = q; 
      
      if (item.breakage > item.quantity) {
        // Prevent breakage from exceeding gross quantity visually, but let the user correct it
      }

      // Volume in cubic meters. Example 650x240x100 mm => 0.65 * 0.24 * 0.100 = 0.0156
      item.quantityTotal = q * (item.length / 1000) * (item.width / 1000) * (item.height / 1000);
      
      totalItem += q;
    }

    this.form.patchValue({
      totalItem: totalItem
    }, { emitEvent: false });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      alert('Please fill all required wire cutting fields before saving.');
      return;
    }

    this.calculateTotals();

    const userId = this.auth.getLoggedInUserId();
    const currentTime = this.getCurrentTime();

    if (!this.form.value.time) {
      this.form.patchValue({ time: currentTime });
    }

    // Use getRawValue() to include disabled fields
    const payload = {
      ...this.form.getRawValue(),
      sizeDetails: this.sizeDetails,
      userId,
      branchId: 1,
      orgId: 1
    };

    console.log('WIRE CUTTING PAYLOAD', payload);

    const req$ = this.editId
      ? this.service.update(this.editId, payload)
      : this.service.save(payload);

    req$.subscribe({
      next: () => {
        this.showForm = false;
        this.editId = null;
        this.load(true);
      },
      error: (err) => {
        console.error('Wire cutting save failed', err);
        alert(err?.error?.message || err?.error?.error || 'Failed to save wire cutting report.');
      }
    });
  }

  get noBatchAvailable(): boolean {
    return this.productionList.length === 0;
  }
  cancel() {
    this.showForm = false;
    this.editId = null;
  }

  // ================= MODAL =================
  openModal(r: any) {
    this.selected = r;
    const el = document.getElementById('wireCuttingModal');
    if (!el) return;
    new bootstrap.Modal(el).show();
  }

  closeModal() {
    const el = document.getElementById('wireCuttingModal');
    if (!el) return;
    bootstrap.Modal.getInstance(el)?.hide();
  }
  getApprovalLevels(r: any) {
    return {
      checkedBy: {
        name: r?.approvedByL1 || '—',
        level: r?.approvedByL1 ? 'L1' : ''
      },
      reviewedBy: {
        name: r?.approvedByL2 || '—',
        level: r?.approvedByL2 ? 'L2' : ''
      },
      approvedBy: {
        name: r?.approvedByL3 || '—',
        level: r?.approvedByL3 ? 'L3' : ''
      }
    };
  }


  approve() {
    if (!this.selected || this.selected.id == null) {
      alert('No record selected');
      return;
    }

    this.service.approve(this.selected.id).subscribe(() => {
      alert('Approved successfully');
      this.load();
    });
  }


  reject() {
    if (!this.selected || this.selected.id == null) {
      alert('No record selected');
      return;
    }

    const reason = prompt('Enter rejection reason');
    if (!reason) return;

    this.service.reject(this.selected.id, reason).subscribe(() => {
      alert('Rejected successfully');
      this.load();
    });
  }

  private toBackendDate(value: any): string {
    if (!value) return '';

    // already yyyy-MM-dd
    if (typeof value === 'string' && value.includes('-')) {
      return value;
    }

    // dd/MM/yyyy → yyyy-MM-dd
    const parts = value.split('/');
    if (parts.length === 3) {
      const [dd, mm, yyyy] = parts;
      return `${yyyy}-${mm}-${dd}`;
    }

    return '';
  }


  // ================= EXPORT =================
  formatDate(d: any) {
    return d ? new Date(d).toLocaleDateString('en-GB') : '';
  }

  exportPDF() {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }
    this.workflowService.exportReport('WIRE_CUTTING', this.filterFromDate, this.filterToDate, 'pdf').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `WireCutting_Report_${this.filterFromDate}_to_${this.filterToDate}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export PDF')
    });
  }


  private excelFieldConfig = [

    // ===== COMMON (once) =====
    { label: 'Batch No', key: 'batchNo' },
    { label: 'Production Date', key: 'createdDate', format: 'date' },
    { label: 'Shift', key: 'shift' },

    // ===== PRODUCTION =====
    { label: 'Silo No 1', key: 'siloNo1' },
    { label: 'FA Density (L/W)', key: 'literWeight1' },
    { label: 'FA Solid 1', key: 'faSolid1' },

    { label: 'Silo No 2', key: 'siloNo2' },
    { label: 'Excess Density', key: 'literWeight2' },
    { label: 'FA Solid 2', key: 'faSolid2' },

    { label: 'Total Solid', key: 'totalSolid' },
    { label: 'Water Liter', key: 'waterLiter' },
    { label: 'Cement Kg', key: 'cementKg' },
    { label: 'Lime Kg', key: 'limeKg' },
    { label: 'Gypsum Kg', key: 'gypsumKg' },
    { label: 'Sol Oil Kg', key: 'solOilKg' },
    { label: 'AI Power (gm)', key: 'aiPowerGm' },
    { label: 'Temperature (°C)', key: 'tempC' },

    { label: 'Casting Time', key: 'castingTime' },
    { label: 'Production Time', key: 'productionTime' },
    { label: 'Production Remark', key: 'productionRemark' },

    // ===== CASTING =====
    { label: 'Size', key: 'size' },
    { label: 'Mould No', key: 'mouldNo' },
    { label: 'Flow (cm)', key: 'flowInCm' },
    { label: 'Casting Temp (°C)', key: 'castingTempC' },
    { label: 'Casting Remark', key: 'remark' },

    // ===== WIRE CUTTING =====
    { label: 'Cutting Date', key: 'cuttingDate', format: 'date' },
    { label: 'Mould No', key: 'mouldNo' },
    { label: 'Size', key: 'size' },
    { label: 'Penetration (mm)', key: 'ballTestMm' },
    { label: 'Time', key: 'time' },
    { label: 'Remark', key: 'remark' },

    // ===== APPROVAL =====
    { label: 'Approval Stage', key: 'approvalStage' },
    { label: 'Approved By L1', key: 'approvedByL1' },
    { label: 'Approved By L2', key: 'approvedByL2' },
    { label: 'Approved By L3', key: 'approvedByL3' }
  ];

  exportExcel() {
    const data = this.filteredList.map((r: any) => ({
      CuttingDate: r.cuttingDate ?? '',
      PlantName: r.plantName ?? '',
      BatchNo: r.batchNo ?? '',
      Shift: r.shift ?? '',
      MouldNo: r.mouldNo ?? '',
      Size: r.size ?? '',
      BallTestMm: r.ballTestMm ?? '',
      Time: r.time ?? '',
      SizeDetails: (r.sizeDetails || []).map((sd: any) => `Thickness: ${sd.height}, Qty: ${sd.quantity}, Breakage: ${sd.breakage}, Net: ${sd.netQuantity}`).join(' | '),
      TotalItem: r.totalItem ?? '',
      CuttingTempC: r.cuttingTempC ?? '',
      CuttingHours: r.cuttingHours ?? '',
      Remark: r.remark ?? ''
    }));

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'WireCutting');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `WireCutting_Report_${this.filterFromDate || 'all'}_to_${this.filterToDate || 'all'}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
  }




  onExportChange(event: Event) {
    const value = (event.target as HTMLSelectElement).value;
    if (value === 'pdf') this.exportPDF();
    if (value === 'excel') this.exportExcel();
    if (value === 'horizontal') this.exportHorizontalReport();
    (event.target as HTMLSelectElement).value = '';
  }

  // ================= DOWNLOAD SINGLE =================
  download(format: string = 'pdf') {
    const r = this.selected;
    if (!r || !r.batchNo) { alert('No batch selected to download'); return; }
    this.workflowService.downloadReport(r.batchNo, 'CUTTING', format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const ext = format === 'excel' ? 'xlsx' : 'pdf';
        a.download = `workflow_report_${r.batchNo}_CUTTING.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { alert('Failed to download report. Ensure previous stages are completed.'); }
    });
  }

  /** Download combined horizontal Excel for selected wire cutting batch */
  downloadHorizontalReport(r?: any) {
    const batchNo = r?.batchNo || this.selected?.batchNo;
    if (!batchNo) { alert('No batch number available'); return; }
    this.horizontalReportService.downloadLifecycleExcel(batchNo, 'CUTTING').subscribe({
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

  /** Export horizontal report for the selected date range */
  exportHorizontalReport() {
    if (!this.filterFromDate || !this.filterToDate) { alert('Please select a date range first'); return; }
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'CUTTING', this.filterPlant, this.filterShift).subscribe({
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

  loadCurrentUserRole() {
    const role = localStorage.getItem('role') || '';
    this.currentUserRole = role.startsWith('ROLE_')
      ? role
      : `ROLE_${role}`;
  }

  canViewWireCutting(r: any): boolean {
    if (!r) return false;

    const stage = r.approvalStage || 'NONE';

    switch (this.currentUserRole) {

      case 'ROLE_DIRECTOR':
        return stage === 'NONE'; // pending + rejected

      case 'ROLE_MANAGER':
        return stage === 'L1';

      case 'ROLE_SUPERVISOR':
        return stage === 'L2' || stage === 'L3';

      case 'ROLE_COMPANY_OWNER':
      case 'ROLE_ADMIN':
      case 'ROLE_USER':
        return true;

      default:
        return false;
    }
  }

  canApproveWireCutting(r: any): boolean {
    if (!r) return false;

    const stage = r.approvalStage || 'NONE';

    return (
      (this.currentUserRole === 'ROLE_DIRECTOR' && stage === 'NONE') ||
      (this.currentUserRole === 'ROLE_MANAGER' && stage === 'L1') ||
      (this.currentUserRole === 'ROLE_SUPERVISOR' && stage === 'L2')
    );
  }

  canRejectWireCutting(r: any): boolean {
    if (!r) return false;

    const stage = r.approvalStage;

    if (stage === 'L3') return false;

    return (
      (this.currentUserRole === 'ROLE_DIRECTOR' && stage === 'NONE') ||
      (this.currentUserRole === 'ROLE_MANAGER' && stage === 'L1') ||
      (this.currentUserRole === 'ROLE_SUPERVISOR' && stage === 'L2')
    );
  }
  private excelToDtoMap: Record<string, string> = {
    'Batch No': 'batchNo',
    'Cutting Date': 'cuttingDate',
    'Mould No': 'mouldNo',
    'Size': 'size',
    'Penetration (mm)': 'ballTestMm',
    'Time': 'time',
    'Qty 100': 'qty100',
    'Qty 150': 'qty150',
    'Breakage 100': 'breakage100',
    'Breakage 150': 'breakage150',
    'Remark': 'remark'
  };

  onImportSelect(event: Event) {
    const value = (event.target as HTMLSelectElement).value;

    if (value === 'excel') {
      const fileInput = document.getElementById('wireCuttingExcelInput') as HTMLInputElement;
      fileInput?.click();
    }

    (event.target as HTMLSelectElement).value = '';
  }
  private wireCuttingExcelColumns = [
    'Batch No',
    'Cutting Date',
    'Mould No',
    'Size',
    'Penetration (mm)',
    'Time',
    'Qty 100',
    'Qty 150',
    'Breakage 100',
    'Breakage 150',
    'Remark'
  ];

  onExcelSelect(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    this.showImportModal = true;

    const reader = new FileReader();

    reader.onload = (e: any) => {
      const workbook = XLSX.read(e.target.result, { type: 'binary' });
      const sheet = workbook.Sheets[workbook.SheetNames[0]];

      const rawRows: any[] = XLSX.utils.sheet_to_json(sheet, {
        defval: '',
        raw: false
      });

      if (!rawRows.length) {
        alert('Excel is empty');
        return;
      }

      // ✅ SHOW ONLY WIRE CUTTING COLUMNS IN PREVIEW
      this.importColumns = this.wireCuttingExcelColumns;

      // ✅ MAP ONLY WIRE CUTTING DATA
      this.importPreviewList = rawRows.map(row => {
        const dto: WireCuttingReport = {
          batchNo: row['Batch No'],
          plantName: row['Plant Name'] || this.filterPlant,
          cuttingDate: this.toBackendDate(row['Cutting Date']),
          mouldNo: Number(row['Mould No']),
          size: String(row['Size']),
          ballTestMm: Number(row['Penetration (mm)']),
          time: String(row['Time']),
          qty100: Number(row['Qty 100']) || 0,
          qty150: Number(row['Qty 150']) || 0,
          breakage100: Number(row['Breakage 100']) || 0,
          breakage150: Number(row['Breakage 150']) || 0,
          remark: row['Remark'],
          userId: 1,
          branchId: 1,
          orgId: 1
        };

        return dto;
      });


      this.importCurrentPage = 1;
      this.updateImportPagination();
    };

    reader.readAsBinaryString(file);
    event.target.value = '';
  }
  colToKey: Record<string, string> = {
    'Batch No': 'batchNo',
    'Cutting Date': 'cuttingDate',
    'Mould No': 'mouldNo',
    'Size': 'size',
    'Ball Test (mm)': 'ballTestMm',
    'Cutting Time': 'time',
    'Qty 100': 'qty100',
    'Qty 150': 'qty150',
    'Breakage 100': 'breakage100',
    'Breakage 150': 'breakage150',
    'Remark': 'remark'
  };

  updateImportPagination() {
    this.importTotalPages = Math.ceil(
      this.importPreviewList.length / this.importPageSize
    );

    const start = (this.importCurrentPage - 1) * this.importPageSize;
    const end = start + this.importPageSize;

    this.pagedImportPreview =
      this.importPreviewList.slice(start, end);
  }

  goToImportPage(p: number) {
    this.importCurrentPage = p;
    this.updateImportPagination();
  }

  nextImportPage() {
    if (this.importCurrentPage < this.importTotalPages) {
      this.importCurrentPage++;
      this.updateImportPagination();
    }
  }

  prevImportPage() {
    if (this.importCurrentPage > 1) {
      this.importCurrentPage--;
      this.updateImportPagination();
    }
  }
  saveImportedWireCutting() {
    if (!this.importPreviewList.length) {
      alert('No data to save');
      return;
    }

    this.service.importWireCutting({
      wireCuttings: this.importPreviewList
    }).subscribe({
      next: () => {
        alert('Wire Cutting imported successfully');
        this.closeImportModal();
        this.load();
      },
      error: () => alert('Import failed')
    });
  }

  closeImportModal() {
    this.showImportModal = false;
    this.importPreviewList = [];
    this.pagedImportPreview = [];
  }

}
