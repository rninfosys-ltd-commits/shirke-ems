import { Component, HostListener, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CastingHallReportService } from '../services/CastingHallReportService';
import { ProductionService } from '../services/ProductionService';
import * as bootstrap from 'bootstrap';
import { Router } from '@angular/router';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';
import { AuthService } from '../services/auth.service';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';
import { BatchLookupService } from '../services/batch-lookup.service';

@Component({
  selector: 'app-casting-hall-report',
  templateUrl: './casting-hall-report.component.html',
  styleUrls: ['./casting-hall-report.component.css']
})
export class CastingHallReportComponent implements OnInit {
  currentUserRole = '';

  showForm = false;
  remarkDropdownOpen = false;
  remarkOptions: { value: string; label: string }[] = [
    { value: 'OK', label: 'OK' },
    { value: 'Needs Review', label: 'Needs Review' },
    { value: 'Hold', label: 'Hold' },
    { value: 'Rework', label: 'Rework' }
  ];
  customRemark = '';
  reportForm!: FormGroup;
  // ================= IMPORT =================
  showImportModal = false;
  // importPreviewList: any[] = [];
  pagedImportPreview: any[] = [];

  importPageSize = 5;
  importCurrentPage = 1;
  importTotalPages = 0;

  reportList: any[] = [];
  filteredList: any[] = [];
  productionList: any[] = [];

  availableProductionList: any[] = [];
  allProductionList: any[] = [];

  // 🔥 NEW: merged export will use this
  mergedExportList: any[] = [];
  // ================= PAGINATION =================
  pageSize = 10;
  currentPage = 1;
  totalElements = 0;
  totalPages = 0;
  plantFilter = 'Plant 1';
  shiftFilter = '';
  // pagedList: any[] = [];


  // selectedCasting: any = null;

  editId: number | null = null;
  importColumns: string[] = [];   // Excel headers (unique)
  importPreviewList: any[] = [];  // Excel rows

  rawMaterialTotals: any = {};

  filterFromDate = '';
  filterToDate = '';
  selectedCasting: any = null;

  batchers: string[] = ['S. S. Bhosale', 'S. J. Bhosale', 'R. M. Swami', 'P. D. Vanjare'];
  shifts: string[] = [
    'Night (00:00 - 08:00) [1st Shift]',
    'Morning (08:00 - 16:00) [2nd Shift]',
    'Afternoon (16:00 - 00:00) [3rd Shift]'
  ];

  constructor(
    private fb: FormBuilder,
    private service: CastingHallReportService,
    private productionService: ProductionService,
    private auth: AuthService,
    private router: Router,
    private workflowService: WorkflowService,
    private filterService: FilterService,
    private horizontalReportService: HorizontalReportService,
    private batchLookup: BatchLookupService
  ) { }

  // ================= INIT =================
  ngOnInit(): void {
    this.loadCurrentUserRole(); // 🔥 REQUIRED

    const today = new Date().toISOString().substring(0, 10);

    this.reportForm = this.fb.group({
      reportDate: [today],
      shift: ['', Validators.required],
      plantName: ['Plant 1', Validators.required],
      batchNo: ['', Validators.required],
      height: [''],
      mouldNo: [0],
      flowInCm: [0],
      mouldHeight: [0],
      mouldFlow: [0],
      remark: ['OK']
    });

    this.setShiftByTime();
    this.loadReports();
    this.loadProductionBatches();

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
    if (hour >= 0 && hour < 8) this.reportForm.patchValue({ shift: this.shifts[0] });
    else if (hour >= 8 && hour < 16) this.reportForm.patchValue({ shift: this.shifts[1] });
    else this.reportForm.patchValue({ shift: this.shifts[2] });
  }

  normalizeRemarkValue(value: string | null | undefined): string {
    const trimmed = (value ?? '').toString().trim();
    return trimmed ? trimmed : 'OK';
  }

  getRemarkSelectionValue(value: string | null | undefined): string {
    const trimmed = (value ?? '').toString().trim();
    if (!trimmed) return 'custom';
    return this.remarkOptions.some(option => option.value === trimmed) ? 'preset' : 'custom';
  }

  toggleRemarkSelection(value: string): void {
    this.reportForm.patchValue({ remark: value });
    this.customRemark = '';
    this.remarkDropdownOpen = false;
  }

  onRemarkInput(event: Event): void {
    const target = event.target as HTMLInputElement;
    const value = target.value;
    this.customRemark = value;
    this.reportForm.patchValue({ remark: this.normalizeRemarkValue(value) });
  }

  toggleRemarkDropdown(event: Event): void {
    event.stopPropagation();
    this.remarkDropdownOpen = !this.remarkDropdownOpen;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target.closest('.remark-dropdown')) {
      this.remarkDropdownOpen = false;
    }
  }
  private buildMergedExportData() {
    return this.filteredList.map(casting => {

      const production = this.allProductionList.find(
        p => p.batchNo === casting.batchNo
      ) || {};

      // 🔥 merge production + casting
      return {
        ...production,
        ...casting
      };
    });
  }
  getCurrentTime(): string {
    const now = new Date();

    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');

    return `${hh}:${mm}:${ss} `;
  }
  goToDashboard() {
    this.router.navigate(['/production-dashboard']);
  }

  // ================= LOAD =================
  loadProductionBatches() {
    this.productionService.getAll().subscribe(res => {
      this.allProductionList = res;          // ✅ keep all
      this.filterAvailableBatches();         // ✅ calculate available
    });
  }


  loadReports() {
    this.service.getAll(this.currentPage - 1, this.pageSize, this.plantFilter).subscribe(res => {
      this.reportList = res.content;
      this.totalElements = res.totalElements;
      this.totalPages = res.totalPages;

      // Since we are doing server-side pagination, filteredList is just reportList
      this.filteredList = this.reportList;

      this.computeTotals();
      this.filterAvailableBatches();
    });
  }

  computeTotals(): void {
      this.rawMaterialTotals = {
          faSolid1: 0,
          totalSolid: 0,
          faSlurryQty: 0,
          excessSlurryQty: 0,
          waterLiter: 0,
          cementKg: 0,
          limeKg: 0,
          gypsumKg: 0,
          solOilKg: 0,
          surfactant: 0,
          aluminumPowderKg: 0,
          dcmrt: 0
      };

      for (const row of this.reportList) {
          const prod = row.productionEntry || {};
          this.rawMaterialTotals.faSolid1 += Number(prod['faSolid1'] || 0);
          this.rawMaterialTotals.totalSolid += Number(prod['totalSolid'] || 0);
          this.rawMaterialTotals.faSlurryQty += Number(prod['faSlurryQty'] || 0);
          this.rawMaterialTotals.excessSlurryQty += Number(prod['excessSlurryQty'] || 0);
          this.rawMaterialTotals.waterLiter += Number(prod['waterLiter'] || 0);
          this.rawMaterialTotals.cementKg += Number(prod['cementKg'] || 0);
          this.rawMaterialTotals.limeKg += Number(prod['limeKg'] || 0);
          this.rawMaterialTotals.gypsumKg += Number(prod['gypsumKg'] || 0);
          this.rawMaterialTotals.solOilKg += Number(prod['solOilKg'] || 0);
          this.rawMaterialTotals.surfactant += Number(prod['surfactant'] || 0);
          this.rawMaterialTotals.aluminumPowderKg += Number(prod['aluminumPowderKg'] || 0);
          this.rawMaterialTotals.dcmrt += Number(prod['dcmrt'] || 0);
      }
  }

  formatTotal(value: number): string {
      return isNaN(value) ? '0' : value.toFixed(2).replace(/\.00$/, '');
  }


  filterAvailableBatches() {
    if (!this.allProductionList.length) return;

    const usedBatchNos = this.reportList.map(r => r.batchNo);
    const selectedPlant = this.reportForm?.get('plantName')?.value;

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
    this.reportForm.patchValue({ batchNo: '' });
    this.filterAvailableBatches();
  }

  onBatchChange(event?: any) {
    const batchNo = this.reportForm.get('batchNo')?.value;
    if (batchNo) {
      this.batchLookup.getBatchDetails(batchNo).subscribe({
        next: (res) => {
          const shared = res?.sharedFields || {};
          if (res && res.production) {
            this.reportForm.patchValue({
              reportDate: res.production.createdDate ? new Date(res.production.createdDate).toISOString().substring(0, 10) : new Date().toISOString().substring(0, 10),
              shift: shared.shift || res.production.shift || this.reportForm.value.shift,
              plantName: shared.plantName || res.production.plantName || this.reportForm.value.plantName
            });

            if (this.reportForm.contains('mouldNo')) {
              this.reportForm.patchValue({ mouldNo: shared.mouldNo ?? this.reportForm.value.mouldNo });
            }
            if (this.reportForm.contains('flowInCm')) {
              this.reportForm.patchValue({ flowInCm: shared.flowInCm ?? this.reportForm.value.flowInCm });
            }
            if (this.reportForm.contains('mouldHeight')) {
              this.reportForm.patchValue({ mouldHeight: shared.mouldHeight ?? this.reportForm.value.mouldHeight });
            }
            if (this.reportForm.contains('mouldFlow')) {
              this.reportForm.patchValue({ mouldFlow: shared.mouldFlow ?? this.reportForm.value.mouldFlow });
            }
          }
        },
        error: (err) => console.log('Lookup error:', err)
      });
    }
  }


  // ================= FILTER =================
  applyFilters() {
    // Note: The date range filtering from filterService is still client-side if reportList has enough data,
    // but the task specifically asked for server-side pagination and plant filtering.
    // If date range is also needed on server, the API would need more parameters.
    // For now, I'll focus on the requested plantName filter and pagination.

    this.currentPage = 1;
    this.loadReports();
  }

  onFilterChange(event: any) {
    this.plantFilter = event.target.value;
    this.applyFilters();
  }

  onShiftFilterChange(event: any) {
    this.shiftFilter = event.target.value;
    this.applyFilters();
  }

  goToPage(page: number) {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.loadReports();
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadReports();
    }
  }

  prevPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadReports();
    }
  }


  onDateChange() {
    this.filterService.setFromDate(this.filterFromDate);
    this.filterService.setToDate(this.filterToDate);
  }

  clearFilters() {
    this.filterFromDate = '';
    this.filterToDate = '';
    this.plantFilter = 'Plant 1';
    this.onDateChange();
  }

  openForm() {
    this.showForm = true;
    this.editId = null;
    this.customRemark = '';
    this.remarkDropdownOpen = false;

    this.reportForm.reset({
      reportDate: new Date().toISOString().substring(0, 10),
      plantName: 'Plant 1',
      mouldNo: 0,
      flowInCm: 0,
      mouldHeight: 0,
      mouldFlow: 0,
      remark: 'OK'
    });
    this.setShiftByTime();

    this.productionList = [...this.availableProductionList];
  }


  edit(row: any) {
    this.editId = row.id;
    this.showForm = true;
    this.customRemark = '';
    this.remarkDropdownOpen = false;

    const rowData = { ...row };
    // fallback to createdDate if reportDate is not present
    let dateVal = rowData.reportDate || rowData.createdDate;
    if (dateVal) {
      if (typeof dateVal === 'string' && dateVal.includes('T')) {
        rowData.reportDate = dateVal.substring(0, 10);
      } else {
        rowData.reportDate = new Date(dateVal).toISOString().substring(0, 10);
      }
    }

    const initialRemark = this.normalizeRemarkValue(rowData.remark);
    this.reportForm.patchValue({
      ...rowData,
      remark: initialRemark
    });
    this.customRemark = this.getRemarkSelectionValue(initialRemark) === 'custom' ? initialRemark : '';

    // ✅ EDIT MODE → show ALL batches matching the plant
    const selectedPlant = row.plantName;
    this.productionList = this.allProductionList.filter(p => !selectedPlant || p.plantName === selectedPlant);
  }


  delete(id: number) {
    if (confirm('Delete this casting report?')) {
      this.service.delete(id).subscribe(() => this.loadReports());
    }
  }

  submit() {

    const userId = this.auth.getLoggedInUserId();

    const currentTime = this.getCurrentTime();

    const finalRemark = this.normalizeRemarkValue(this.reportForm.value.remark);
    const payload = {
      ...this.reportForm.value,
      remark: finalRemark,
      userId,
      branchId: 1,
      orgId: 1
    };

    if (payload.shift) {
      payload.shift = payload.shift.split(' ')[0];
    }

    const req$ = this.editId
      ? this.service.update(this.editId, payload)
      : this.service.save(payload);

    req$.subscribe(() => {
      this.showForm = false;
      this.editId = null;
      this.loadReports();
    });
  }


  cancel() {
    this.showForm = false;
    this.editId = null;
  }

  get noBatchAvailable(): boolean {
    return !this.productionList || this.productionList.length === 0;
  }

  openCastingModal(r: any) {
    this.selectedCasting = r;

    const modalEl = document.getElementById('castingModal');

    console.log('modal element:', modalEl);   // ADD THIS

    if (!modalEl) return;

    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }


  closeCastingModal() {
    const modalEl = document.getElementById('castingModal');
    if (!modalEl) return;

    const modalInstance = bootstrap.Modal.getInstance(modalEl);
    modalInstance?.hide();
  }


  // ================= EXPORT =================
  formatDate(date: any): string {
    return date ? new Date(date).toLocaleDateString('en-GB') : '';
  }

  exportPDF() {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }

    this.workflowService.exportReport('CASTING', this.filterFromDate, this.filterToDate, 'pdf').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Casting_Report_${this.filterFromDate}_to_${this.filterToDate}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export PDF')
    });
  }



  private excelFieldConfig = [

    // ===== COMMON (once) =====
    { label: 'Batch No', key: 'batchNo' },
    { label: 'Production Date', key: 'reportDate', format: 'date' },
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
    { label: 'Height', key: 'height' },
    { label: 'Mould No', key: 'mouldNo' },
    { label: 'Flow (cm)', key: 'flowInCm' },
    { label: 'Casting Remark', key: 'remark' },

    // ===== APPROVAL =====
    { label: 'Approval Stage', key: 'approvalStage' },
    { label: 'Approved By L1', key: 'approvedByL1' },
    { label: 'Approved By L2', key: 'approvedByL2' },
    { label: 'Approved By L3', key: 'approvedByL3' }
  ];


  exportExcel() {
    const data = this.filteredList.map(r => ({
      ReportDate: r.reportDate ?? '',
      PlantName: r.plantName ?? '',
      BatchNo: r.batchNo ?? '',
      Shift: r.shift ?? '',
      Height: r.height ?? '',
      MouldNo: r.mouldNo ?? '',
      FlowInCm: r.flowInCm ?? '',
      MouldHeight: r.mouldHeight ?? '',
      MouldFlow: r.mouldFlow ?? '',
      Remark: r.remark ?? ''
    }));

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Casting');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Casting_Report_${this.filterFromDate || 'all'}_to_${this.filterToDate || 'all'}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
  }



  onExportChange(event: any) {
    const value = event.target.value;
    if (value === 'excel') this.exportExcel();
    // if (value === 'pdf') this.exportPdf(); // Not implemented in this component yet
    if (value === 'horizontal') this.exportHorizontalReport();
    event.target.value = '';
  }

  onImportSelect(event: any) {
    const value = event.target.value;
    if (value === 'excel') {
      const fileInput = document.getElementById('castingExcelInput') as HTMLInputElement;
      fileInput?.click();
    }
    event.target.value = '';
  }

  getCastingApprovalLevels(c: any) {
    return {
      checkedBy: {
        name: c?.approvedByL1 || '',
        level: c?.approvedByL1 ? 'L1' : ''
      },
      reviewedBy: {
        name: c?.approvedByL2 || '',
        level: c?.approvedByL2 ? 'L2' : ''
      },
      approvedBy: {
        name: c?.approvedByL3 || '',
        level: c?.approvedByL3 ? 'L3' : ''
      }
    };
  }




  canApproveCasting(c: any): boolean {
    if (!c) return false;

    const stage = c.approvalStage || 'NONE';

    return (
      (this.currentUserRole === 'ROLE_DIRECTOR' && stage === 'NONE') ||
      (this.currentUserRole === 'ROLE_MANAGER' && stage === 'L1') ||
      (this.currentUserRole === 'ROLE_SUPERVISOR' && stage === 'L2')
    );
  }

  canRejectCasting(c: any): boolean {
    if (!c) return false;

    const stage = c.approvalStage;

    if (stage === 'L3') return false;

    return (
      (this.currentUserRole === 'ROLE_DIRECTOR' && stage === 'NONE') ||
      (this.currentUserRole === 'ROLE_MANAGER' && stage === 'L1') ||
      (this.currentUserRole === 'ROLE_SUPERVISOR' && stage === 'L2')
    );
  }


  approveCasting() {

    this.service.approve(this.selectedCasting.id).subscribe(() => {
      alert('Approved successfully');
      this.reloadSelectedCasting();
      this.loadReports();
    });
  }

  rejectCasting() {

    const reason = prompt('Enter rejection reason');
    if (!reason) return;

    this.service.reject(this.selectedCasting.id, reason).subscribe(() => {
      alert('Rejected successfully');
      this.reloadSelectedCasting();
      this.loadReports();
    });
  }





  reloadSelectedCasting() {
    this.service.getById(this.selectedCasting.id).subscribe(res => {
      this.selectedCasting = res;
    });
  }
  downloadCasting(format: string = 'pdf') {
    if (!this.selectedCasting || !this.selectedCasting.batchNo) {
      alert('No batch selected to download');
      return;
    }
    this.workflowService.downloadReport(this.selectedCasting.batchNo, 'CASTING', format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const ext = format === 'excel' ? 'xlsx' : 'pdf';
        a.download = `workflow_report_${this.selectedCasting.batchNo}_CASTING.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Failed to download report. Ensure previous stages are completed.');
      }
    });
  }

  /** Download combined horizontal Excel for selected casting batch */
  downloadHorizontalReport(r?: any) {
    const batchNo = r?.batchNo || this.selectedCasting?.batchNo;
    if (!batchNo) { alert('No batch number available'); return; }
    this.horizontalReportService.downloadLifecycleExcel(batchNo, 'CASTING').subscribe({
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
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'CASTING', this.plantFilter, this.shiftFilter).subscribe({
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
  private castingPdfFields = [
    { label: 'Batch No', key: 'batchNo' },
    { label: 'Date', key: 'reportDate', format: 'date' },

    { label: 'Height', key: 'height' },
    { label: 'Mould No', key: 'mouldNo' },

    { label: 'Flow (cm)', key: 'flowInCm' },
    { label: 'Remark', key: 'remark' },

    { label: 'Approved By L1', key: 'approvedByL1' },
    { label: 'Approved By L2', key: 'approvedByL2' },
    { label: 'Approved By L3', key: 'approvedByL3' }
  ];
  canViewCasting(c: any): boolean {
    if (!c) return false;

    const stage = c.approvalStage || 'NONE';

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




  loadCurrentUserRole() {
    const role = (localStorage.getItem('role') || '').trim();
    this.currentUserRole = role.startsWith('ROLE_')
      ? role
      : `ROLE_${role}`;
  }
  openImportModal() {
    this.showImportModal = true;
  }

  closeImportModal() {
    this.showImportModal = false;
    this.importPreviewList = [];
    this.pagedImportPreview = [];
  }

  // 🔥 Excel → DTO field mapping
  private excelToDtoMap: Record<string, string> = {
    'Batch No': 'batchNo',
    'Size': 'height',
    'Mould No': 'mouldNo',
    'Flow (cm)': 'flowInCm',
    'Casting Remark': 'remark'
  };
  importPreviewFields = [
    { label: 'Batch No', key: 'batchNo' },
    { label: 'Height', key: 'height' },
    { label: 'Mould No', key: 'mouldNo' },
    { label: 'Flow (cm)', key: 'flowInCm' },
    { label: 'Remark', key: 'remark' }
  ];

  onExcelSelect(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    this.showImportModal = true;   // OPEN MODAL FIRST

    const reader = new FileReader();

    reader.onload = (e: any) => {
      const workbook = XLSX.read(e.target.result, { type: 'binary' });
      const sheet = workbook.Sheets[workbook.SheetNames[0]];

      // 1️⃣ Read raw rows
      const rawRows: any[] = XLSX.utils.sheet_to_json(sheet, {
        defval: '',
        raw: false
      });

      if (!rawRows.length) {
        alert('Excel file is empty');
        return;
      }

      // 2️⃣ Extract & REMOVE duplicate columns
      const columnSet = new Set<string>();

      rawRows.forEach(row => {
        Object.keys(row).forEach(key => columnSet.add(key.trim()));
      });

      this.importPreviewList = rawRows.map(row => {
        const dto: any = {};

        Object.keys(this.excelToDtoMap).forEach(excelCol => {
          const dtoField = this.excelToDtoMap[excelCol];
          dto[dtoField] = row[excelCol] ?? null;
        });

        // 🔥 REQUIRED DEFAULTS
        dto.userId = 1;
        dto.branchId = 1;
        dto.orgId = 1;

        return dto;
      });


      // 4️⃣ Pagination
      this.importCurrentPage = 1;
      this.updateImportPagination();
    };

    reader.readAsBinaryString(file);

    // reset input so same file can be re-selected
    event.target.value = '';
  }



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
  saveImportedCasting() {
    if (!this.importPreviewList.length) {
      alert('No data to save');
      return;
    }

    const payload = {
      castings: this.importPreviewList
    };


    this.service.importCasting(payload).subscribe({
      next: (res) => {
        alert(`Saved: ${res.savedCount}, Failed: ${res.errorCount} `);

        this.closeImportModal();
        this.loadReports();
      },
      error: () => {
        alert('Import failed');
      }
    });
  }


}
