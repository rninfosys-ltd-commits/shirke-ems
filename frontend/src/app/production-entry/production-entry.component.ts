import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductionService } from '../services/ProductionService';
import { MaterialMasterService, MaterialMaster } from '../services/MaterialMasterService';
import * as bootstrap from 'bootstrap';
import { ProductionImportResponse } from '../models/ProductionImportResponse';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import * as XLSX from 'xlsx';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { WorkflowService } from '../services/workflow.service';
import { HorizontalReportService } from '../services/horizontal-report.service';
import { BatcherService } from '../services/batcher.service';

@Component({
  selector: 'app-production-entry',
  templateUrl: './production-entry.component.html',
  styleUrls: ['./production-entry.component.css']
})
export class ProductionEntryComponent implements OnInit {

  currentUserRole = '';

  selectedProduction: any = null;

  productionForm!: FormGroup;

  productionList: any[] = [];
  filteredProductionList: any[] = [];

  showForm = false;
  editId: number | null = null;

  siloList: number[] = [];
  showImportModal = false;
  excelHeaders: string[] = [];
  excelRows: any[] = [];

  filterFromDate = '';
  filterToDate = '';
  filterPlant = 'Plant 1';
  filterShift = '';
  excelPreview: any[] = [];
  hasExcelErrors = false;
  apiMessage = '';

  // ================= PAGINATION =================
  pageSize = 5;
  currentPage = 1;
  totalPages = 0;
  pagedProductionList: any[] = [];

  // ================= DYNAMIC MATERIALS =================
  materialList: MaterialMaster[] = [];
  materialValues: { [materialId: number]: number } = {};

  // ================= LIVE CALCULATIONS =================
  liveCalc = {
    totalSolid: 0,
    totalBatchWeight: 0,
    totalLiquid: 0,
    finalSolidPercent: 0,
    totalSolidsPerCbm: 0,
    totalBindersPerCbm: 0,
    totalWaterPerCbm: 0,
    waterSolidRatio: 0
  };

  batchers: any[] = [];
  filteredBatchers: any[] = [];
  showBatcherDropdown = false;
  shifts: string[] = [
    'Night (00:00 - 08:00) [1st Shift]',
    'Morning (08:00 - 16:00) [2nd Shift]',
    'Afternoon (16:00 - 00:00) [3rd Shift]'
  ];

  constructor(
    private fb: FormBuilder,
    private service: ProductionService,
    private materialService: MaterialMasterService,
    private auth: AuthService,
    private router: Router,
    private workflowService: WorkflowService,
    private horizontalReportService: HorizontalReportService,
    private batcherService: BatcherService
  ) { }

  ngOnInit(): void {

    console.log('ROLE IN COMPONENT:', this.currentUserRole);

    this.loadCurrentUserRole();

    this.siloList = Array.from({ length: 5 }, (_, i) => i + 1);

    const today = new Date().toISOString().substring(0, 10);

    this.productionForm = this.fb.group({
      plantName: ['Plant 1', Validators.required],
      shift: ['', Validators.required],
      productionDate: [today],
      batcherName: [''],
      batcherId: [null],

      siloNo1: [''],
      faSolid1: [''],

      // Slurry Properties
      faDensity: [0],
      excessDensity: [1.4],
      excessSolid: [0],

      // Other Ingredients
      faSlurryQty: [0],
      excessSlurryQty: [0],
      waterLiter: [0],
      limeKg: [0],
      cementKg: [0],
      gypsumKg: [0],
      solOilKg: [0],
      surfactant: [0],
      aluminumPowderKg: [0],
      dcmrt: [0],

      // Process Parameters
      mixingTime: [180],
      tempC: [0],
      castingTime: [''],
      productionTime: [''],
      productionRemark: [''],
      cbmVolume: [''],

      userId: [1],
      branchId: [1],
      orgId: [1]
    });

    // Reset CBM Volume when Plant changes
    this.productionForm.get('plantName')?.valueChanges.subscribe(() => {
      this.productionForm.patchValue({ cbmVolume: '' });
      this.recalculate();
    });

    this.setShiftByTime();
    this.loadData();
    this.loadUsers();
    this.loadMaterials();
    this.loadBatchers();
  }

  loadBatchers() {
    this.batcherService.getAllBatchers().subscribe({
      next: (data) => {
        this.batchers = data || [];
        this.filteredBatchers = [...this.batchers];
      },
      error: () => {
        console.warn('Failed to load batchers');
      }
    });
  }


  loadCurrentUserRole() {
    const role = localStorage.getItem('role') || '';

    this.currentUserRole = role.startsWith('ROLE_')
      ? role
      : `ROLE_${role}`;

    console.log('ROLE IN COMPONENT:', this.currentUserRole);
  }

  loadUsers() {
    this.auth.getAllUsers().subscribe(users => {
      users.forEach((u: any) => {
        this.userMap[String(u.id)] = u.username;
      });
      console.log('USER MAP:', this.userMap);
    });
  }

  // ================= LOAD DYNAMIC MATERIALS =================
  loadMaterials() {
    this.materialService.getAll().subscribe({
      next: (materials) => {
        this.materialList = materials || [];
        // Initialize values map
        this.materialList.forEach(m => {
          this.materialValues[m.id] = 0;
        });
        console.log('MATERIALS LOADED:', this.materialList);
      },
      error: (err) => {
        console.warn('Material master API not available, using legacy mode:', err.status);
        this.materialList = [];
      }
    });
  }


  openProductionModal(p: any) {
    this.selectedProduction = p;
    this.loadCurrentUserRole();
    const modalEl = document.getElementById('productionModal');
    if (!modalEl) return;

    const modal = new bootstrap.Modal(modalEl);
    modal.show();
  }

  loadData() {
    this.service.getAll().subscribe(res => {
      this.productionList = res || [];
      this.applyFilters();
      this.updatePagination();
    });
  }

  applyFilters() {
    if (
      this.filterFromDate &&
      this.filterToDate &&
      new Date(this.filterToDate) < new Date(this.filterFromDate)
    ) {
      alert('To Date cannot be earlier than From Date');
      return;
    }

    const from = this.filterFromDate
      ? new Date(this.filterFromDate).getTime()
      : null;

    const to = this.filterToDate
      ? new Date(this.filterToDate + 'T23:59:59').getTime()
      : null;

    this.filteredProductionList = this.productionList.filter(p => {

      // ✅ PLANT FILTER (handle both 'Plant 1' and '1')
      if (this.filterPlant) {
        const plantId = this.filterPlant.replace('Plant ', '');
        if (p.plantName !== this.filterPlant && p.plantName !== plantId) return false;
      }

      // ✅ SHIFT FILTER
      if (this.filterShift && !(p.shift || '').toLowerCase().includes(this.filterShift.toLowerCase())) {
        return false;
      }

      // ✅ DATE FILTER
      const date = new Date(p.createdDate).getTime();
      const dateOk =
        (!from || date >= from) &&
        (!to || date <= to);

      if (!dateOk) return false;

      // ✅ ROLE FILTER
      const stage = p.approvalStage || 'NONE';

      switch (this.currentUserRole) {
        case 'ROLE_DIRECTOR':
          return true;

        case 'ROLE_MANAGER':
          return stage === 'L1';

        case 'ROLE_SUPERVISOR':
          return stage === 'L2';

        case 'ROLE_COMPANY_OWNER':
        case 'ROLE_ADMIN':
        case 'ROLE_USER':
          return true;

        default:
          return false;
      }
    });

    this.currentPage = 1;
    this.updatePagination();
  }

  onShiftChange() {
    this.applyFilters();
  }

  updatePagination() {
    this.totalPages = Math.ceil(
      this.filteredProductionList.length / this.pageSize
    );

    const start = (this.currentPage - 1) * this.pageSize;
    const end = start + this.pageSize;

    this.pagedProductionList =
      this.filteredProductionList.slice(start, end);
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



  clearFilters() {
    this.filterFromDate = '';
    this.filterToDate = '';
    this.filterPlant = 'Plant 1';
    this.filterShift = '';
    this.filteredProductionList = [...this.productionList];
    this.currentPage = 1;
    this.updatePagination();
  }

  exportData(type: string) {

    // 🔥 force close import modal before export
    this.showImportModal = false;
    this.excelPreview = [];

    if (type === 'pdf') this.exportPDF();
    if (type === 'excel') this.exportExcel();
  }


  exportPDF() {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }

    this.workflowService.exportReport('PRODUCTION', this.filterFromDate, this.filterToDate, 'pdf', this.filterPlant || undefined).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Production_Report_${this.filterFromDate}_to_${this.filterToDate}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export PDF')
    });
  }


  formatDate(date: any): string {
    if (!date) return '';
    return new Date(date).toLocaleDateString('en-GB'); // dd/MM/yyyy
  }


  exportExcel() {
    const data = this.filteredProductionList.map(p => {
      const row: any = {
        PlantName: p.plantName ?? '',
        Shift: p.shift ?? '',
        ProductionDate: p.productionDate ?? '',
        BatcherName: p.batcherName ?? '',
        BatcherId: p.batcherId ?? '',
        SiloNo1: p.siloNo1 ?? '',
        FaSolid1: p.faSolid1 ?? '',
        FaDensity: p.faDensity ?? '',
        ExcessDensity: p.excessDensity ?? '',
        ExcessSolid: p.excessSolid ?? '',
        FaSlurryQty: p.faSlurryQty ?? '',
        ExcessSlurryQty: p.excessSlurryQty ?? '',
        WaterLiter: p.waterLiter ?? '',
        LimeKg: p.limeKg ?? '',
        CementKg: p.cementKg ?? '',
        GypsumKg: p.gypsumKg ?? '',
        SolOilKg: p.solOilKg ?? '',
        Surfactant: p.surfactant ?? '',
        AluminumPowderKg: p.aluminumPowderKg ?? '',
        Dcmrt: p.dcmrt ?? '',
        MixingTime: p.mixingTime ?? '',
        TempC: p.tempC ?? '',
        CastingTime: p.castingTime ?? '',
        ProductionTime: p.productionTime ?? '',
        ProductionRemark: p.productionRemark ?? '',
        CbmVolume: p.cbmVolume ?? '',
        TotalSolid: p.totalSolid ?? '',
        TotalSolidsPerCbm: p.totalSolidsPerCbm ?? '',
        TotalBindersPerCbm: p.totalBindersPerCbm ?? '',
        TotalWaterPerCbm: p.totalWaterPerCbm ?? '',
        WaterSolidRatio: p.waterSolidRatio ?? ''
      };

      (p.materials || []).forEach((m: any) => {
        row[m.materialName || `Material_${m.materialMasterId}`] = m.value ?? '';
      });

      return row;
    });

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Production');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Production_Report_${this.filterFromDate || 'all'}_to_${this.filterToDate || 'all'}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
  }



  calculateTotalSolid(): number {
    return (+this.productionForm.value.faSolid1 || 0);
  }

  recalculate(): void {
    const v = this.productionForm.value;

    const faDensity = +v.faDensity || 0;
    const faSolid1 = +v.faSolid1 || 0;
    const excessDensity = +v.excessDensity || 0;
    const excessSolid = +v.excessSolid || 0;

    const faSlurryQty = +v.faSlurryQty || 0;
    const excessSlurryQty = +v.excessSlurryQty || 0;
    const waterLiter = +v.waterLiter || 0;
    const limeKg = +v.limeKg || 0;
    const cementKg = +v.cementKg || 0;
    const gypsumKg = +v.gypsumKg || 0;
    const solOilKg = +v.solOilKg || 0;
    const surfactant = +v.surfactant || 0;
    const aluminumPowderKg = +v.aluminumPowderKg || 0;
    const dcmrt = (+v.dcmrt || 0) / 1000; // ml -> kg

    // Solid contributions
    const faSolidFromSlurry = faSlurryQty * (faSolid1 / 100);
    const excessSolidFromSlurry = excessSlurryQty * (excessSolid / 100);
    const solidIngredients = limeKg + cementKg + gypsumKg;

    const totalSolid = faSolidFromSlurry + excessSolidFromSlurry + solidIngredients;

    // Liquid contributions (water weight)
    const faLiquid = faSlurryQty - faSolidFromSlurry;
    const excessLiquid = excessSlurryQty - excessSolidFromSlurry;
    const totalLiquid = faLiquid + excessLiquid + waterLiter + solOilKg + surfactant;

    // Total batch weight
    const totalBatchWeight = faSlurryQty + excessSlurryQty + waterLiter + limeKg + cementKg + gypsumKg + solOilKg + surfactant + aluminumPowderKg + dcmrt;

    // Final solid %
    const finalSolidPercent = totalBatchWeight > 0 ? (totalSolid / totalBatchWeight) * 100 : 0;

    const cbmVolume = +v.cbmVolume || 0;
    const totalSolidsPerCbm = cbmVolume > 0 ? (totalSolid / cbmVolume) : 0;
    const totalBindersPerCbm = cbmVolume > 0 ? ((cementKg + limeKg) / cbmVolume) : 0;
    const totalWaterPerCbm = cbmVolume > 0 ? (totalLiquid / cbmVolume) : 0;
    const waterSolidRatio = totalSolid > 0 ? (totalLiquid / totalSolid) : 0;

    this.liveCalc = {
      totalSolid: +totalSolid.toFixed(2),
      totalBatchWeight: +totalBatchWeight.toFixed(2),
      totalLiquid: +totalLiquid.toFixed(2),
      finalSolidPercent: +finalSolidPercent.toFixed(2),
      totalSolidsPerCbm: +totalSolidsPerCbm.toFixed(2),
      totalBindersPerCbm: +totalBindersPerCbm.toFixed(2),
      totalWaterPerCbm: +totalWaterPerCbm.toFixed(2),
      waterSolidRatio: +waterSolidRatio.toFixed(2)
    };
  }

  getOperatorName(): string {
    const userId = this.auth.getLoggedInUserId();
    if (!userId) return '—';
    return this.getUserName(userId) || '—';
  }

  userMap: { [key: string]: string } = {};


  getUserName(userId: any): string {
    if (!userId) return '—';

    const key = String(userId);

    return this.userMap[key] || key;
  }




  setShiftByTime() {
    const hour = new Date().getHours();
    if (hour >= 0 && hour < 8) this.productionForm.patchValue({ shift: this.shifts[0] });
    else if (hour >= 8 && hour < 16) this.productionForm.patchValue({ shift: this.shifts[1] });
    else this.productionForm.patchValue({ shift: this.shifts[2] });
  }

  // ================= SEARCHABLE DROPDOWN LOGIC =================
  toggleBatcherDropdown(show: boolean) {
    // Delay hiding to allow click event to register on items
    if (!show) {
      setTimeout(() => {
        this.showBatcherDropdown = false;
      }, 200);
    } else {
      this.showBatcherDropdown = true;
      this.filterBatchers();
    }
  }

  onBatcherInput() {
    this.showBatcherDropdown = true;
    this.filterBatchers();
  }

  filterBatchers() {
    const val = this.productionForm.value.batcherName || '';
    this.filteredBatchers = this.batchers.filter(b =>
      b.name.toLowerCase().includes(val.toLowerCase())
    );
  }

  selectBatcher(b: any) {
    this.productionForm.patchValue({ batcherName: b.name, batcherId: b.id });
    this.showBatcherDropdown = false;
  }

  getInitials(name: string): string {
    if (!name) return '??';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  }

  openForm() {
    this.showForm = true;
    this.editId = null;

    const today = new Date().toISOString().substring(0, 10);

    this.productionForm.reset({
      plantName: 'Plant 1',
      productionDate: today,
      productionTime: this.getCurrentTime(),
      excessDensity: 1.4,
      mixingTime: 180,
      faDensity: 0,
      faSolid1: 0,
      excessSolid: 0,
      faSlurryQty: 0,
      excessSlurryQty: 0,
      waterLiter: 0,
      limeKg: 0,
      cementKg: 0,
      gypsumKg: 0,
      solOilKg: 0,
      surfactant: 0,
      aluminumPowderKg: 0,
      dcmrt: 0,
      tempC: 0,
      cbmVolume: 4.5 // Default commonly used CBM Volume
    });
    this.liveCalc = {
      totalSolid: 0,
      totalBatchWeight: 0,
      totalLiquid: 0,
      finalSolidPercent: 0,
      totalSolidsPerCbm: 0,
      totalBindersPerCbm: 0,
      totalWaterPerCbm: 0,
      waterSolidRatio: 0
    };
    this.filteredBatchers = [...this.batchers];

    // Reset dynamic material values
    this.materialList.forEach(m => {
      this.materialValues[m.id] = 0;
    });

    this.setShiftByTime();
  }

  getCurrentTime(): string {
    const now = new Date();

    let hours: number | string = now.getHours();
    let minutes: number | string = now.getMinutes();
    let seconds: number | string = now.getSeconds();

    hours = hours < 10 ? '0' + hours : hours;
    minutes = minutes < 10 ? '0' + minutes : minutes;
    seconds = seconds < 10 ? '0' + seconds : seconds;

    return `${hours}:${minutes}:${seconds}`;
  }

  submit() {
    const userId = this.auth.getLoggedInUserId();

    if (!userId) {
      alert('User not logged in');
      return;
    }

    // Validate plant selection
    const plantVal = this.productionForm.value.plantName;
    if (!plantVal) {
      alert('Please select a Plant');
      return;
    }

    const currentTime = this.getCurrentTime();

    this.productionForm.patchValue({
      productionTime: currentTime
    });

    // Build dynamic materials array
    const materialsPayload = this.materialList.map(m => ({
      materialMasterId: m.id,
      materialName: m.materialName,
      unit: m.unit,
      value: this.materialValues[m.id] || 0,
      displayOrder: m.displayOrder
    }));

    const payload = {
      ...this.productionForm.value,
      batcherId: this.productionForm.value.batcherId,
      batcherName: this.productionForm.value.batcherName,
      userId,
      totalSolid: this.liveCalc.totalSolid || this.calculateTotalSolid(),
      totalSolidsPerCbm: this.liveCalc.totalSolidsPerCbm,
      totalBindersPerCbm: this.liveCalc.totalBindersPerCbm,
      totalWaterPerCbm: this.liveCalc.totalWaterPerCbm,
      waterSolidRatio: this.liveCalc.waterSolidRatio,
      materials: materialsPayload
    };

    const req$ = this.editId
      ? this.service.update(this.editId, payload)
      : this.service.save(payload);

    req$.subscribe(() => {
      this.showForm = false;
      this.loadData();
    });
  }


  edit(row: any) {
    this.editId = row.id;
    this.showForm = true;
    this.productionForm.patchValue(row);

    // Populate dynamic material values from saved data
    this.materialList.forEach(m => {
      this.materialValues[m.id] = 0; // reset
    });

    if (row.materials && row.materials.length) {
      row.materials.forEach((pm: any) => {
        if (pm.materialMasterId) {
          this.materialValues[pm.materialMasterId] = pm.value || 0;
        }
      });
    }

    this.filteredBatchers = [...this.batchers];

    // Recalculate so the live calc panel shows values instead of 0
    setTimeout(() => this.recalculate(), 100);
  }

  delete(id: number) {
    if (confirm('Delete this production entry?')) {
      this.service.delete(id).subscribe(() => this.loadData());
    }
  }

  getApprovalLevels(p: any) {
    return {
      checkedBy: {
        name: this.getUserName(p?.approvedByL1),
        level: p?.approvedByL1 ? 'Director' : ''
      },
      reviewedBy: {
        name: this.getUserName(p?.approvedByL2),
        level: p?.approvedByL2 ? 'Manager' : ''
      },
      approvedBy: {
        name: this.getUserName(p?.approvedByL3),
        level: p?.approvedByL3 ? 'Supervisor' : ''
      }
    };
  }



  private buildExportRows(p: any): any[] {
    return this.getFieldConfig().map(f => {
      let value = p?.[f.key];

      if (f.format === 'date' && value) {
        value = this.formatDate(value);
      }

      return [
        f.label,
        value !== null && value !== undefined && value !== '' ? value : ''
      ];
    });
  }

  // Dynamic field config that includes materials
  getFieldConfig(): { label: string; key: string; format?: string }[] {
    const baseConfig = [
      { label: 'Batch No', key: 'batchNo' },
      { label: 'Date', key: 'createdDate', format: 'date' },
      { label: 'Shift', key: 'shift' },

      { label: 'Silo No 1', key: 'siloNo1' },
      { label: 'FA Solid 1', key: 'faSolid1' },

      { label: 'Total Solid', key: 'totalSolid' },
      { label: 'FA Slurry Qty', key: 'faSlurryQty' },
      { label: 'Excess Slurry Qty', key: 'excessSlurryQty' },
      { label: 'Surfactant', key: 'surfactant' },
      { label: 'Aluminium Powder (gm)', key: 'aluminumPowderKg' },
      { label: 'DC MRT', key: 'dcmrt' },
      { label: 'Mixing Time', key: 'mixingTime' },
    ];

    // Add dynamic material columns
    this.materialList.forEach(m => {
      baseConfig.push({
        label: m.materialName,
        key: `material_${m.id}`
      });
    });

    const tailConfig = [
      { label: 'Casting Time', key: 'castingTime' },
      { label: 'Production Time', key: 'productionTime' },

      { label: 'Production Remark', key: 'productionRemark' },

      { label: 'Approval Stage', key: 'approvalStage' },
      { label: 'Approved By L1', key: 'approvedByL1' },
      { label: 'Approved By L2', key: 'approvedByL2' },
      { label: 'Approved By L3', key: 'approvedByL3' }
    ];

    return [...baseConfig, ...tailConfig];
  }

  // Helper to get material value from a production entry for display
  getMaterialValue(production: any, materialId: number): any {
    if (!production || !production.materials) return '—';
    const mat = production.materials.find((m: any) => m.materialMasterId === materialId);
    return mat ? mat.value : '—';
  }


  downloadProduction(format: string = 'pdf') {
    if (!this.selectedProduction || !this.selectedProduction.batchNo) {
      alert('No batch selected to download');
      return;
    }

    this.workflowService.downloadReport(this.selectedProduction.batchNo, 'PRODUCTION', format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const ext = format === 'excel' ? 'xlsx' : 'pdf';
        a.download = `workflow_report_${this.selectedProduction.batchNo}_PRODUCTION.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Failed to download report.');
      }
    });
  }

  /** Download combined horizontal Excel for one batch */
  downloadHorizontalReport(batchNo: string) {
    if (!batchNo) { alert('No batch number available'); return; }
    this.horizontalReportService.downloadLifecycleExcel(batchNo, 'PRODUCTION').subscribe({
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
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select a date range first');
      return;
    }
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'PRODUCTION', this.filterPlant || undefined).subscribe({
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


  canApprove(p: any): boolean {
    if (!p) return false;

    const stage = p.approvalStage || 'NONE';

    return (
      (this.currentUserRole === 'ROLE_DIRECTOR' && stage === 'NONE') ||
      (this.currentUserRole === 'ROLE_MANAGER' && stage === 'L1') ||
      (this.currentUserRole === 'ROLE_SUPERVISOR' && stage === 'L2')
    );
  }


  canReject(p: any): boolean {
    if (!p) return false;

    const stage = p.approvalStage;

    if (stage === 'L3') return false;

    return (
      (this.currentUserRole === 'ROLE_DIRECTOR' && stage === 'NONE') ||
      (this.currentUserRole === 'ROLE_MANAGER' && stage === 'L1') ||
      (this.currentUserRole === 'ROLE_SUPERVISOR' && stage === 'L2')
    );
  }


  canEditDelete(): boolean {
    return (
      this.currentUserRole === 'ROLE_COMPANY_OWNER' ||
      this.currentUserRole === 'ROLE_ADMIN' ||
      this.currentUserRole === 'ROLE_DIRECTOR'
    );
  }

  approveProduction() {
    if (!this.selectedProduction) return;

    const userId = this.auth.getLoggedInUserId();
    if (!userId) return;

    this.service.approve(
      this.selectedProduction.id,
      userId,
      this.currentUserRole
    ).subscribe(() => {
      alert('Approved successfully');
      this.closeModal();
      this.loadData();
    });
  }


  rejectProduction() {
    if (!this.selectedProduction) return;

    const reason = prompt('Enter rejection reason');
    if (!reason) return;

    const userId = this.auth.getLoggedInUserId();
    if (!userId) return;

    this.service.reject(
      this.selectedProduction.id,
      reason,
      userId,
      this.currentUserRole
    ).subscribe(() => {
      alert('Rejected successfully');
      this.closeModal();
      this.loadData();
    });
  }

  closeModal() {
    const modalEl = document.getElementById('productionModal');
    if (!modalEl) return;

    const modalInstance = bootstrap.Modal.getInstance(modalEl);
    modalInstance?.hide();
  }



  canViewProduction(p: any): boolean {
    if (!p) return false;

    const stage = p.approvalStage || 'NONE';

    switch (this.currentUserRole) {

      case 'ROLE_DIRECTOR':
        return stage === 'NONE';

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

  importExcel(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();

    reader.onload = (e: any) => {
      const workbook = XLSX.read(e.target.result, { type: 'binary' });
      const sheetName = workbook.SheetNames[0];
      const sheet = workbook.Sheets[sheetName];

      const rows = XLSX.utils.sheet_to_json<any>(sheet, {
        defval: ''
      });

      if (!rows.length) {
        alert('Excel file is empty');
        return;
      }

      this.excelHeaders = Object.keys(rows[0]);

      this.excelRows = rows.map(r => ({
        ...r,
        importStatus: 'PENDING',
        errorMessage: ''
      }));

      this.showImportModal = true;
    };

    reader.readAsBinaryString(file);
  }


  normalizeRow(row: any) {
    const obj: any = {};
    Object.keys(row).forEach(k => {
      const key = k.toLowerCase().replace(/\s+/g, '').replace(/_/g, '');
      obj[key] = row[k];
    });
    return obj;
  }

  formatExcelDate(value: any): string | null {
    if (!value) return null;

    if (typeof value === 'number') {
      const d = XLSX.SSF.parse_date_code(value);
      return `${d.y}-${String(d.m).padStart(2, '0')}-${String(d.d).padStart(2, '0')}`;
    }

    if (typeof value === 'string' && value.includes('/')) {
      const [d, m, y] = value.split('/');
      return `${y}-${m.padStart(2, '0')}-${d.padStart(2, '0')}`;
    }

    return value;
  }

  saveExcelToDB() {
    const payload = {
      productions: this.excelRows.map(r => ({
        siloNo1: r['Silo No 1'] || r['Silo No'],
        faSolid1: Number(r['FA Solid 1'] || r['FA Solid'] || 0),

        waterLiter: Number(r['Water Liter'] || 0),
        cementKg: Number(r['Cement Kg'] || 0),
        limeKg: Number(r['Lime Kg'] || 0),
        gypsumKg: Number(r['Gypsum Kg'] || 0),
        solOilKg: Number(r['Sol Oil Kg'] || 0),
        aiPowerGm: Number(r['AI Power gm'] || 0),
        tempC: Number(r['Temperature (°C)'] || 0),

        castingTime: r['Casting Time'],
        productionTime: r['Production Time'],
        productionRemark: r['Production Remark']
      })),
      uploadedBy: 1,
      branchId: 1,
      orgId: 1
    };

    this.service.importProduction(payload).subscribe(res => {

      this.excelRows.forEach((row, index) => {
        row.importStatus = res.results[index]?.status;
        row.errorMessage = res.results[index]?.error || '';
      });

      this.apiMessage =
        `${res.savedCount} saved, ${res.errorCount} failed`;

      this.showImportModal = false;
      this.loadData();
      this.currentPage = 1;
    });
  }




  clearExcelPreview() {
    this.excelPreview = [];
    this.hasExcelErrors = false;
    this.apiMessage = '';
    this.showImportModal = false;
  }

  onImportSelect(event: any, fileInput: HTMLInputElement) {
    if (event.target.value === 'excel') fileInput.click();
    event.target.value = '';
  }

  goToDashboard() {
    this.router.navigate(['/production-dashboard']);
  }

}
