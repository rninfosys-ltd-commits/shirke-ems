import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BlockSeparatingService } from '../services/BlockSeparatingService';
// import { BlockSeparatingService } from '../services/block-separating.service';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';
import { BatchLookupService } from '../services/batch-lookup.service';

@Component({
  selector: 'app-block-separating',
  templateUrl: './block-separating.component.html',
  styleUrls: ['./block-separating.component.css']
})
export class BlockSeparatingComponent implements OnInit {

  form!: FormGroup;
  isSubmitting = false;
  isEdit = false;
  filterFromDate = '';
  filterToDate = '';
  filterPlant = 'Plant 1';
  autoclaveBatches: any[] = [];
  // ================= PAGINATION =================
  pageSize = 5;        // records per page
  currentPage = 1;
  totalPages = 0;
  pagedList: any[] = [];
  editId: number | null = null;

  shifts: string[] = [
    'Night (00:00 - 08:00) [1st Shift]',
    'Morning (08:00 - 16:00) [2nd Shift]',
    'Afternoon (16:00 - 00:00) [3rd Shift]'
  ];

  filterShift = '';
  filterBlockSize = '';

  blockSizes: string[] = [];
  usedBatchNumbers: string[] = [];

  showForm = false;

  list: any[] = [];          // full data
  filteredList: any[] = [];  // table data

  constructor(
    private fb: FormBuilder,
    private service: BlockSeparatingService,
    private auth: AuthService,
    private router: Router,
    private workflowService: WorkflowService,
    private filterService: FilterService,
    private horizontalReportService: HorizontalReportService,
    private batchLookup: BatchLookupService
  ) { }

  onBatchChange(event?: any): void {
    const batchNumber = this.form.get('batchNumber')?.value;
    if (!batchNumber) return;

    // Auto-fill castingDate from the already-loaded batch list
    const matched = this.autoclaveBatches.find(b => b.batchNo === batchNumber);
    if (matched?.castingDate) {
      const dateStr = new Date(matched.castingDate).toISOString().split('T')[0];
      this.form.patchValue({ castingDate: dateStr });
    }

    this.batchLookup.getBatchDetails(batchNumber).subscribe({
      next: (res) => {
        const shared = res?.sharedFields || {};
        const src = res?.cutting || res?.casting || res?.production;
        if (src) {
          this.form.patchValue({
            shift: shared.shift || src.shift || this.form.value.shift,
            plantName: shared.plantName || src.plantName || this.form.value.plantName
          });
          // Also fill castingDate from API if not already set
          const apiDate = src.castingDate || src.cuttingDate || src.createdDate;
          if (apiDate && !this.form.value.castingDate) {
            this.form.patchValue({ castingDate: new Date(apiDate).toISOString().split('T')[0] });
          }
        }
        if (shared.blockSize || res?.cutting?.size) {
          this.form.patchValue({ blockSize: shared.blockSize || res.cutting.size });
        }
      },
      error: (err) => console.log('Batch lookup error:', err)
    });
  }

  ngOnInit(): void {
    const today = new Date().toISOString().split('T')[0];

    this.form = this.fb.group({
      reportDate: [today, Validators.required],
      plantName: ['Plant 1', Validators.required],
      batchNumber: ['', Validators.required],
      castingDate: ['', Validators.required],
      blockSize: ['', Validators.required],
      shift: ['', Validators.required],
      time: ['', Validators.required],
      remark: ['']
    });

    this.setShiftByTime();
    this.loadList();
    // this.loadCuttingBatches();   // ✅ correct method

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

  edit(row: any) {
    this.showForm = true;
    this.isEdit = true;
    this.editId = row.id;

    // 🔥 Filter batches by the plant of the row being edited
    const selectedPlant = row.plantName;
    let filtered = this.allCuttingReports.filter(r => r.plantName === selectedPlant);

    this.autoclaveBatches = filtered.map(r => ({
      batchNo: r.batchNo,
      castingDate: r.cuttingDate
    }));

    // If the current batch is not in the filtered list (e.g. it was already used), add it
    if (!this.autoclaveBatches.find(b => b.batchNo === row.batchNumber)) {
      this.autoclaveBatches.unshift({
        batchNo: row.batchNumber,
        castingDate: row.castingDate
      });
    }

    let formattedReportDate = row.reportDate;
    if (formattedReportDate) {
      if (typeof formattedReportDate === 'string' && formattedReportDate.includes('T')) {
        formattedReportDate = formattedReportDate.substring(0, 10);
      } else {
        formattedReportDate = new Date(formattedReportDate).toISOString().substring(0, 10);
      }
    } else if (row.createdDate) {
      formattedReportDate = new Date(row.createdDate).toISOString().substring(0, 10);
    }

    let formattedCastingDate = row.castingDate;
    if (formattedCastingDate) {
      if (typeof formattedCastingDate === 'string' && formattedCastingDate.includes('T')) {
        formattedCastingDate = formattedCastingDate.substring(0, 10);
      } else {
        formattedCastingDate = new Date(formattedCastingDate).toISOString().substring(0, 10);
      }
    }

    this.form.patchValue({
      reportDate: formattedReportDate,
      plantName: row.plantName,
      batchNumber: row.batchNumber,
      castingDate: formattedCastingDate,
      blockSize: row.blockSize,
      shift: row.shift,
      time: row.time,
      remark: row.remark
    });
  }

  goToDashboard() {
    this.router.navigate(['/production-dashboard']);
  }

  getCurrentTime(): string {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }
  loadList(preservePage = false) {
    this.service.getAll().subscribe(res => {

      const map = new Map<string, any>();
      res.forEach((r: any) => map.set(r.batchNumber, r));

      this.list = Array.from(map.values());
      this.filteredList = [...this.list];

      this.usedBatchNumbers = this.list.map(r => r.batchNumber);

      this.blockSizes = [
        ...new Set(this.list.map(r => r.blockSize).filter(Boolean))
      ];

      if (!preservePage) {
        this.currentPage = 1;
      }
      this.updatePagination();      // 🔥 ADD THIS

      this.loadCuttingBatches();    // dropdown refresh
    });
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



  allCuttingReports: any[] = [];

  loadCuttingBatches() {
    this.service.getCuttingBatches().subscribe({
      next: (res: any[]) => {
        this.allCuttingReports = res || [];
        this.filterBatchesByPlant();
      },
      error: (err: any) => {
        console.error('Batch load error:', err);
        alert('Failed to load batches');
      }
    });
  }

  filterBatchesByPlant() {
    const selectedPlant = this.form?.get('plantName')?.value;
    let filtered = this.allCuttingReports.filter(r => !this.usedBatchNumbers.includes(r.batchNo));

    if (selectedPlant) {
      filtered = filtered.filter(r => r.plantName === selectedPlant);
    }

    this.autoclaveBatches = filtered.map(r => ({
      batchNo: r.batchNo,
      castingDate: r.cuttingDate
    }));
  }

  onPlantChange() {
    this.form.patchValue({ batchNumber: '', castingDate: '' });
    this.filterBatchesByPlant();
  }





  openForm() {
    this.showForm = true;
    this.isEdit = false;

    const today = new Date().toISOString().split('T')[0];

    this.form.reset({
      reportDate: today,
      plantName: 'Plant 1',
      time: this.getCurrentTime()   // 🔥 AUTO TIME
    });
    this.setShiftByTime();
  }
  resetFormWithDefaults() {
    const today = new Date().toISOString().split('T')[0];

    this.form.reset({
      reportDate: today,
      plantName: 'Plant 1',
      shift: ''     // or 'G' if you want default General
    });
  }

  cancel() {
    this.showForm = false;
    this.editId = null;
    this.isEdit = false;

    const today = new Date().toISOString().split('T')[0];
    this.form.reset({ reportDate: today, plantName: 'Plant 1' });
  }


  // ================= SUBMIT =================
  submit() {

    if (!this.form.value.time) {
      this.form.patchValue({
        time: this.getCurrentTime()
      });
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      const missing: string[] = [];
      if (this.form.get('batchNumber')?.invalid) missing.push('Batch Number');
      if (this.form.get('castingDate')?.invalid) missing.push('Casting Date');
      if (this.form.get('blockSize')?.invalid) missing.push('Block Size');
      if (this.form.get('shift')?.invalid) missing.push('Shift');
      if (missing.length) {
        alert('Please fill required fields: ' + missing.join(', '));
      }
      return;
    }

    this.isSubmitting = true;
    const userId = this.auth.getLoggedInUserId();


    const now = this.getCurrentTime();

    // ✅ NEW record or Edit mode → if time empty then take current time
    if (!this.form.value.time) {
      this.form.patchValue({
        time: now
      });
    }

    const payload = {
      ...this.form.value,
      userId: userId,
      branchId: 1,
      orgId: 1
    };

    if (payload.shift) {
      payload.shift = payload.shift.split(' ')[0];
    }

    const request$ = this.editId
      ? this.service.update(this.editId, payload)
      : this.service.create(payload);

    request$.subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showForm = false;
        this.editId = null;
        this.isEdit = false;

        this.loadList(true);
      },
      error: () => {
        alert('Error while saving');
        this.isSubmitting = false;
      }
    });
  }

  get noBatchAvailable(): boolean {
    return this.autoclaveBatches.length === 0;
  }

  delete(id: number) {
    if (!confirm('Are you sure you want to delete this record?')) return;

    this.service.delete(id).subscribe({
      next: () => {
        this.loadList();
      },
      error: () => {
        alert('Delete failed');
      }
    });
  }

  applyFilters() {
    this.filteredList = this.list.filter(r => {

      let ok = true;

      if (this.filterFromDate) {
        ok = ok && new Date(r.castingDate) >= new Date(this.filterFromDate);
      }

      if (this.filterToDate) {
        ok = ok && new Date(r.castingDate) <= new Date(this.filterToDate);
      }

      if (this.filterShift) {
        ok = ok && r.shift === this.filterShift;
      }

      if (this.filterPlant) {
        ok = ok && r.plantName === this.filterPlant;
      }

      if (this.filterBlockSize) {
        ok = ok && r.blockSize === this.filterBlockSize;
      }

      return ok;
    });

    this.currentPage = 1;        // 🔥 RESET PAGE
    this.updatePagination();     // 🔥 IMPORTANT
  }




  onDateChange() {
    this.filterService.setFromDate(this.filterFromDate);
    this.filterService.setToDate(this.filterToDate);
  }

  clearFilters() {
    this.filterFromDate = '';
    this.filterToDate = '';
    this.filterShift = '';
    this.filterBlockSize = '';
    this.filterPlant = 'Plant 1';
    this.onDateChange();
  }


  exportExcel() {
    const data = this.filteredList.map(r => ({
      ReportDate: r.reportDate ?? '',
      PlantName: r.plantName ?? '',
      BatchNumber: r.batchNumber ?? '',
      CastingDate: r.castingDate ?? '',
      BlockSize: r.blockSize ?? '',
      Shift: r.shift ?? '',
      Time: r.time ?? '',
      Remark: r.remark ?? '',
      Remarks: r.remarks ?? '',
      StartTime: r.startTime ?? '',
      EndTime: r.endTime ?? '',
      Duration: r.duration ?? '',
      Operator: r.operator ?? '',
      AutoclaveId: r.autoclaveId ?? '',
      MiddleCrack: r.middleCrack ?? '',
      RisingCrack: r.risingCrack ?? '',
      CornerDamage: r.cornerDamage ?? '',
      BottomLineMiddleCrack: r.bottomLineMiddleCrack ?? '',
      UpperLineCrack: r.upperLineCrack ?? '',
      AutoclaveDamage: r.autoclaveDamage ?? '',
      SideCrack: r.sideCrack ?? '',
      Chipping: r.chipping ?? '',
      CraneDamage: r.craneDamage ?? '',
      Unrise: r.unrise ?? '',
      Unsize: r.unsize ?? '',
      Uncut: r.uncut ?? '',
      Collapse: r.collapse ?? '',
      TotalBreakage: r.totalBreakage ?? '',
      TotalPcs: r.totalPcs ?? '',
      BreakagePercent: r.breakagePercent ?? ''
    }));

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'BlockSeparating');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `BlockSeparating_Report_${this.filterFromDate || 'all'}_to_${this.filterToDate || 'all'}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
  }


  onImportSelect(event: any) {
    if (event.target.value === 'excel') {
      const fileInput = document.getElementById('blockExcelInput') as HTMLInputElement;
      fileInput?.click();
    }
    event.target.value = '';
  }

  onExcelSelect(event: any) {
    alert('Excel import will be added later');
  }

  onExportChange(event: any) {
    const value = event.target.value;
    if (value === 'excel') this.exportExcel();
    if (value === 'pdf') this.exportPdf();
    if (value === 'horizontal') this.exportHorizontalReport();
    event.target.value = '';
  }


  exportPdf() {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }
    this.workflowService.exportReport('BLOCK_SEPARATING', this.filterFromDate, this.filterToDate, 'pdf').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `BlockSeparating_Report_${this.filterFromDate}_to_${this.filterToDate}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export PDF')
    });
  }


  downloadBlock(r: any, format: string = 'pdf') {
    if (!r || !r.batchNumber) { alert('No batch selected to download'); return; }
    this.workflowService.downloadReport(r.batchNumber, 'BLOCK_SEPARATING', format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const ext = format === 'excel' ? 'xlsx' : 'pdf';
        a.download = `workflow_report_${r.batchNumber}_BLOCK_SEPARATING.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { alert('Failed to download report.'); }
    });
  }

  /** Download combined horizontal Excel for this block separating batch */
  downloadHorizontalReport(r: any) {
    const batchNo = r?.batchNumber;
    if (!batchNo) { alert('No batch number available'); return; }
    this.horizontalReportService.downloadLifecycleExcel(batchNo, 'BLOCK_SEPARATING').subscribe({
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
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'BLOCK_SEPARATING', this.filterPlant, this.filterShift).subscribe({
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
