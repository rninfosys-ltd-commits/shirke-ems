import { Component, OnInit, HostListener } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  Validators,
  FormArray
} from '@angular/forms';
import { AutoclaveService } from '../services/AutoclaveService';
import * as XLSX from 'xlsx';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { WireCuttingReportService } from '../services/WireCuttingReportService';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { WorkflowService } from '../services/workflow.service';
import { FilterService } from '../services/filter.service';
import { HorizontalReportService } from '../services/horizontal-report.service';


@Component({
  selector: 'app-autoclave',
  templateUrl: './autoclave.component.html',
  styleUrls: ['./autoclave.component.css']
})
export class AutoclaveComponent implements OnInit {

  // ================= UI STATE =================
  showForm = false;

  // ================= REMARKS MULTI-SELECT UI =================
  remarksDropdownOpen = false;
  selectedRemarks: string[] = [];

  remarksOptions: { value: string; label: string }[] = [
    { value: 'Main mixer discharge valve not opened.', label: 'Main mixer discharge valve not opened. / मुख्य मिक्सर डिस्चार्ज व्हॉल्व्ह उघडलेला नाही.' },
    { value: 'Aluminium mixer valve not opened.', label: 'Aluminium mixer valve not opened. / अॅल्युमिनियम मिक्सर व्हॉल्व्ह उघडलेला नाही.' },
    { value: 'Double aluminium powder added.', label: 'Double aluminium powder added. / दुहेरी प्रमाणात अॅल्युमिनियम पावडर टाकण्यात आली.' },
    { value: 'Soluble oil not added.', label: 'Soluble oil not added. / सोल्युबल ऑइल टाकलेले नाही.' },
    { value: 'Ferry carriage stuck.', label: 'Ferry carriage stuck. / फेरी कॅरेज अडकले आहे.' },
    { value: 'Mixer agitator jammed.', label: 'Mixer agitator jammed. / मिक्सर अॅजिटेटर जॅम झाला आहे.' },
    { value: 'Cement and lime powder screw jammed.', label: 'Cement and lime powder screw jammed. / सिमेंट आणि लाईम पावडर स्क्रू जॅम झाला आहे.' },
    { value: 'Temperature increased due to ferry carriage under maintenance.', label: 'Temperature increased due to ferry carriage under maintenance. / फेरी कॅरेज देखभालीखाली असल्यामुळे तापमान वाढले.' },
    { value: 'Mould derailed on ferry carriage.', label: 'Mould derailed on ferry carriage. / फेरी कॅरेजवर साचा (मोल्ड) रुळावरून घसरला.' },
    { value: 'Heavy leakage bottom side packing escaped.', label: 'Heavy leakage bottom side packing escaped. / खालच्या बाजूची पॅकिंग निघाल्यामुळे मोठी गळती.' },
    { value: 'Mould arm slipped, heavy leakage.', label: 'Mould arm slipped, heavy leakage. / मोल्ड आर्म सटकल्यामुळे मोठी गळती.' }
  ];

  // ================= MAIN FORM =================
  form!: FormGroup;

  // ================= WAGON FORM =================
  wagonForm!: FormGroup;

  // ================= DATA =================
  list: any[] = [];
  filteredList: any[] = [];
  pagedList: any[] = [];

  availableBatches: string[] = [];
  dropdownOpen = false;
  plant1AutoclaveNumbers = [1, 2, 3, 4, 5, 6];
  plant2AutoclaveNumbers = [1, 2, 3, 4, 5];
  totalProcessTimeDisplay = '';

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.dropdownOpen = !this.dropdownOpen;
  }

  getSelectedBatchesArray(): string[] {
    const val = this.form?.get('batchNo')?.value;
    if (!val) return [];
    return val.split(',').map((s: string) => s.trim()).filter((s: string) => s.length > 0);
  }

  isBatchSelected(batch: string): boolean {
    return this.getSelectedBatchesArray().includes(batch);
  }

  toggleBatchSelection(batch: string): void {
    const selected = this.getSelectedBatchesArray();
    const plant = this.form?.get('plantName')?.value;
    const maxLimit = plant === 'Plant 2' ? 14 : 18;

    const index = selected.indexOf(batch);
    if (index > -1) {
      selected.splice(index, 1);
    } else {
      if (selected.length >= maxLimit) {
        alert(`Maximum limit of ${maxLimit} batches reached for ${plant}.`);
        return;
      }
      selected.push(batch);
    }

    const batchNoStr = selected.join(', ');
    this.form.patchValue({
      batchNo: batchNoStr,
      plant1BatchCount: plant === 'Plant 1' ? selected.length : '',
      plant2BatchCount: plant === 'Plant 2' ? selected.length : ''
    });
  }

  getSelectedBatchesPlaceholder(): string {
    const selected = this.getSelectedBatchesArray();
    if (selected.length === 0) {
      return 'Select Batches';
    }
    return `${selected.length} Batches Selected`;
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.custom-multiselect')) {
      this.dropdownOpen = false;
    }
  }


  // ================= FILTER =================
  filterFromDate = '';
  filterToDate = '';
  filterPlant = 'Plant 1';
  filterShift = '';
  // ================= PAGINATION =================
  pageSize = 5;
  currentPage = 1;
  totalPages = 0;
  isEditMode = false;
  selected: any = null;

  // ================= EDIT =================
  editId: number | null = null;

  shifts: string[] = [
    'Night (00:00 - 08:00) [1st Shift]',
    'Morning (08:00 - 16:00) [2nd Shift]',
    'Afternoon (16:00 - 00:00) [3rd Shift]'
  ];

  constructor(
    private fb: FormBuilder,
    private service: AutoclaveService,
    private wireCuttingService: WireCuttingReportService,
    private auth: AuthService,
    private router: Router,
    private workflowService: WorkflowService,
    private filterService: FilterService,
    private horizontalReportService: HorizontalReportService
  ) { }

  // ================= INIT =================
  ngOnInit(): void {
    const today = new Date().toISOString().substring(0, 10);


    // Main Autoclave Form
    this.form = this.fb.group({
      // General Information
      autoclaveCycleNumber: [{value: '', disabled: true}],
      shift: ['', Validators.required],
      plantName: ['Plant 1', Validators.required],
      currentStatus: ['STARTED'],
      startedAt: [''],
      startedDate: [today],
      completedAt: [''],
      completedDate: [''],
      batchNo: [''],

      // New
      autoclaveNumber: ['', Validators.required],

      // Lifecycle Tracking
      doorCloseTime: [''],
      vacuumStartTime: [''],
      autoclaveRisingStartTime: [''],
      autoclaveRisingCloseTime: [''],
      totalPressureAfterRisingClose: [''],
      pressureAfterDoorOpen: [''],

      // Transfer / Release
      transferStartTime: [''],
      transferredToAutoclaveNo: ['', Validators.required],
      transferEndTime: [''],
      releaseStartTime: [''],
      releaseEndTime: [''],

      // Timing
      doorOpenTime: [''],

      // (Removed fields per change request)
      // cycleStartTime, cycleEndTime, holdStartTime, holdEndTime


      // Pressure Readings
      pressure1Hr: [''],
      pressure2Hr: [''],
      pressure3Hr: [''],
      pressureRelease: [''],
      totalProcessTime: [''],

      // Batch Counts
      plant1BatchCount: [''],
      plant2BatchCount: [''],

      remarks: ['']
    });

    this.wagonForm = this.fb.group({
      mBatch: [null],
      mSize: [''],
      eBatch: [null],
      eSize: [''],
      wBatch: [null],
      wSize: [''],
      wagons: this.fb.array([])
    });

    this.form.get('plantName')?.valueChanges.subscribe(() => {
      this.filterBatchesByPlant();
    });

    this.form.get('completedDate')?.valueChanges.subscribe(val => {
      if (val) {
        this.form.patchValue({
          completedAt: this.getCurrentTime()
        });
      }
    });
    this.form.get('doorCloseTime')?.valueChanges.subscribe(() => this.calcTotalProcessTime());
    this.setShiftByTime();
    this.loadCuttingBatches();
    this.loadList();

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
  getCurrentTime(): string {
    const now = new Date();
    const hh = String(now.getHours()).padStart(2, '0');
    const mm = String(now.getMinutes()).padStart(2, '0');
    const ss = String(now.getSeconds()).padStart(2, '0');
    return `${hh}:${mm}:${ss}`;
  }

  calcTotalProcessTime(): void {
    const start = this.form.get('doorCloseTime')?.value;
    const end = this.form.get('releaseEndTime')?.value;
    if (!start || !end) {
      this.totalProcessTimeDisplay = '';
      return;
    }
    const startDate = new Date(start);
    const endDate = new Date(end);
    const diffMs = endDate.getTime() - startDate.getTime();
    if (Number.isNaN(diffMs) || diffMs < 0) {
      this.totalProcessTimeDisplay = '';
      return;
    }
    const totalMinutes = Math.floor(diffMs / 60000);
    const hours = Math.floor(totalMinutes / 60);
    const minutes = totalMinutes % 60;
    this.totalProcessTimeDisplay = `${hours} Hours ${minutes} Minutes`;
  }
  // ================= WAGONS =================
  get wagons(): FormArray {
    return this.wagonForm.get('wagons') as FormArray;
  }

  allCuttingReports: any[] = [];

  loadCuttingBatches(): void {
    this.wireCuttingService.getAll().subscribe(res => {
      this.allCuttingReports = res || [];
      this.availableBatches = [...new Set(this.allCuttingReports.map(r => r.batchNo))];
    });
  }


  filterBatchesByPlant(): void {
    const selectedPlant = this.form?.get('plantName')?.value;
    const plantId = selectedPlant?.replace('Plant ', '');

    let filtered = this.allCuttingReports;
    if (selectedPlant) {
      filtered = this.allCuttingReports.filter(r =>
        r.plantName === selectedPlant || r.plantName === plantId
      );
    }
    this.availableBatches = [...new Set(filtered.map(r => r.batchNo))];
  }

  getAvailableTransferAutoclaves(): number[] {
    const plant = this.form?.get('plantName')?.value;
    const current = Number(this.form?.get('autoclaveNumber')?.value);
    const base = plant === 'Plant 2' ? this.plant2AutoclaveNumbers : this.plant1AutoclaveNumbers;
    return base.filter(no => no !== current);
  }

  onAutoclaveNumberChange(): void {
    const current = Number(this.form?.get('autoclaveNumber')?.value);
    const transferred = Number(this.form?.get('transferredToAutoclaveNo')?.value);
    if (transferred && transferred === current) {
      this.form.patchValue({ transferredToAutoclaveNo: '' });
    }
  }

  onPlantChange(): void {
    this.wagons.clear();
    this.filterBatchesByPlant();
    this.form.patchValue({
      batchNo: '',
      autoclaveNumber: '',
      transferredToAutoclaveNo: '',
      plant1BatchCount: '',
      plant2BatchCount: ''
    });
  }

  addWagon(): void {
    if (this.wagons.length >= 14) return;

    const wagon = this.fb.group({
      mBatch: [this.wagonForm.value.mBatch],
      mSize: [this.wagonForm.value.mSize],
      eBatch: [this.wagonForm.value.eBatch],
      eSize: [this.wagonForm.value.eSize],
      wBatch: [this.wagonForm.value.wBatch],
      wSize: [this.wagonForm.value.wSize]
    });

    this.wagons.push(wagon);

    // clear input fields after add
    this.wagonForm.patchValue({
      mBatch: null,
      mSize: '',
      eBatch: null,
      eSize: '',
      wBatch: null,
      wSize: ''
    });
  }
  goToDashboard() {
    this.router.navigate(['/production-dashboard']);
  }


  removeWagon(index: number): void {
    this.wagons.removeAt(index);
  }

  isBatchUsed(batch: string): boolean {
    return this.wagons.controls.some(ctrl =>
      ctrl.get('batchNo')?.value === batch
    );
  }

  // ================= LOAD =================
  loadList(preservePage = false): void {
    this.service.getAll().subscribe(res => {

      console.log('API RESPONSE:', res);
      console.log('TOTAL RECORDS FROM API:', res.length);

      this.list = res || [];
      this.applyFilters(preservePage);
    });
  }

  applyFilters(preservePage = false): void {
    const from = this.filterFromDate
      ? new Date(this.filterFromDate).getTime()
      : null;

    const to = this.filterToDate
      ? new Date(this.filterToDate + 'T23:59:59').getTime()
      : null;

    this.filteredList = this.list.filter(r => {
      // ✅ PLANT FILTER (handle both 'Plant 1' and '1')
      if (this.filterPlant) {
        const plantId = this.filterPlant.replace('Plant ', '');
        if (r.plantName !== this.filterPlant && r.plantName !== plantId) return false;
      }

      // ✅ SHIFT FILTER
      if (this.filterShift && !(r.shift || '').toLowerCase().includes(this.filterShift.toLowerCase())) return false;

      const d = new Date(r.startedDate).getTime();
      return (!from || d >= from) && (!to || d <= to);
    });

    console.log('FILTERED LIST COUNT:', this.filteredList.length);

    if (!preservePage) {
      this.currentPage = 1;
    }
    this.updatePagination();
  }

  onShiftChange(): void {
    this.applyFilters();
  }


  onDateChange(): void {
    this.filterService.setFromDate(this.filterFromDate);
    this.filterService.setToDate(this.filterToDate);
  }

  clearFilters(): void {
    this.filterFromDate = '';
    this.filterToDate = '';
    this.filterPlant = 'Plant 1';
    this.filterShift = '';
    this.onDateChange();
  }

  // ================= PAGINATION =================
  updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredList.length / this.pageSize);
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedList = this.filteredList.slice(start, start + this.pageSize);
  }

  goToPage(p: number): void {
    this.currentPage = p;
    this.updatePagination();
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.updatePagination();
    }
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updatePagination();
    }
  }

  openForm(): void {

    this.isEditMode = false;
    this.showForm = true;
    this.editId = null;
    const now = this.getCurrentTime();
    this.form.reset({
      startedDate: new Date().toISOString().substring(0, 10),
      plantName: 'Plant 1',
      startedAt: this.getCurrentTime()
    });

    this.form.patchValue({
      startedDate: new Date().toISOString().substring(0, 10),
      startedAt: now,
      completedDate: '',
      completedAt: '',
      autoclaveCycleNumber: '',
      autoclaveNumber: '',
      transferredToAutoclaveNo: '',
      currentStatus: 'STARTED',
      doorCloseTime: '',
      vacuumStartTime: '',
      autoclaveRisingStartTime: '',
      autoclaveRisingCloseTime: '',
      transferStartTime: '',
      transferEndTime: '',
      releaseStartTime: '',
      releaseEndTime: '',
      doorOpenTime: '',
      pressure1Hr: '',
      pressure2Hr: '',
      pressure3Hr: '',
      pressureRelease: '',
      totalPressureAfterRisingClose: '',
      pressureAfterDoorOpen: '',
      totalProcessTime: '',
      plant1BatchCount: '',
      plant2BatchCount: '',
      remarks: '',
      batchNo: ''
    });

    this.wagonForm.reset();
    this.wagons.clear();
    this.setShiftByTime();
  }

  edit(row: any): void {
    this.isEditMode = true;
    this.showForm = true;
    this.editId = row.id;

    this.form.patchValue(row);

    this.wagons.clear();
    if (row.wagons?.length) {
      row.wagons.forEach((w: any) => {
        this.wagons.push(this.fb.group(w));
      });
    }
  }

  save(): void {

    // ✅ If user entered completed date but time still empty
    if (this.form.value.completedDate && !this.form.value.completedAt) {
      this.form.patchValue({
        completedAt: this.getCurrentTime()
      });
    }

    const userId = this.auth.getLoggedInUserId();
    const now = this.getCurrentTime();

    // ✅ NEW record or Edit mode → ensure start time exists
    if (!this.form.value.startedAt) {
      this.form.patchValue({
        startedAt: now
      });
    }

    // ✅ completed date selected but time empty
    if (this.form.value.completedDate && !this.form.value.completedAt) {
      this.form.patchValue({
        completedAt: now
      });
    }

    const payload = {
      ...this.form.value,
      wagons: this.wagonForm.value.wagons,
      userId,
      branchId: 1,
      orgId: 1
    };

    const batchCount = this.getSelectedBatchesArray().length;
    if (this.form.value.plantName === 'Plant 1' && (batchCount < 15 || batchCount > 18)) {
      alert('Plant 1 batch count must be between 15 and 18.');
      return;
    }
    if (this.form.value.plantName === 'Plant 2' && (batchCount < 1 || batchCount > 14)) {
      alert('Plant 2 batch count must be between 1 and 14.');
      return;
    }

    if (payload.shift) {
      payload.shift = payload.shift.split(' ')[0];
    }

    if (payload.autoclaveNumber !== undefined && payload.autoclaveNumber !== null && payload.transferredToAutoclaveNo !== undefined) {
      const current = Number(payload.autoclaveNumber);
      const transferred = Number(payload.transferredToAutoclaveNo);
      if (transferred && transferred === current) {
        alert('Transferred To Autoclave No cannot match the current autoclave number.');
        return;
      }
    }

    console.log('AUTOCLAVE PAYLOAD', payload);

    const req$ = this.editId
      ? this.service.update(this.editId, payload)
      : this.service.save(payload);

    req$.subscribe(() => {
      this.showForm = false;
      this.editId = null;
      this.wagonForm.reset();
      this.wagons.clear();
      this.loadList(true);
    });
  }

  delete(id: number): void {
    if (!id) {
      alert('Unable to delete: missing record id.');
      return;
    }
    if (confirm('Delete this autoclave cycle?')) {
      this.service.delete(id).subscribe({
        next: () => this.loadList(),
        error: (err) => {
          console.error('Autoclave delete failed', err);
          alert('Delete failed. Please check the backend response or record id.');
        }
      });
    }
  }

  // ================= NAV =================
  back(): void {
    this.cancel();
  }

  cancel(): void {
    this.showForm = false;
    this.editId = null;
  }

  // ================= EXPORT =================
  onExportChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    if (value === 'excel') this.exportExcel();
    if (value === 'pdf') this.exportPdf();
    if (value === 'horizontal') this.exportHorizontalReport();
    (event.target as HTMLSelectElement).value = '';
  }

  exportExcel(): void {
    const data = this.filteredList.map(r => ({
      PlantName: r.plantName ?? '',
      AutoclaveCycleNumber: r.autoclaveCycleNumber ?? '',
      Shift: r.shift ?? '',
      StartedAt: r.startedAt ?? '',
      StartedDate: r.startedDate ?? '',
      CompletedAt: r.completedAt ?? '',
      CompletedDate: r.completedDate ?? '',
      BatchNo: r.batchNo ?? '',
      AutoclaveNumber: r.autoclaveNumber ?? '',
      CurrentStatus: r.currentStatus ?? '',
      DoorCloseTime: r.doorCloseTime ?? '',
      VacuumStartTime: r.vacuumStartTime ?? '',
      AutoclaveRisingStartTime: r.autoclaveRisingStartTime ?? '',
      AutoclaveRisingCloseTime: r.autoclaveRisingCloseTime ?? '',
      TransferStartTime: r.transferStartTime ?? '',
      TransferredToAutoclaveNo: r.transferredToAutoclaveNo ?? '',
      TransferEndTime: r.transferEndTime ?? '',
      ReleaseStartTime: r.releaseStartTime ?? '',
      ReleaseEndTime: r.releaseEndTime ?? '',
      DoorOpenTime: r.doorOpenTime ?? '',
      Pressure1Hr: r.pressure1Hr ?? '',
      Pressure2Hr: r.pressure2Hr ?? '',
      Pressure3Hr: r.pressure3Hr ?? '',
      PressureRelease: r.pressureRelease ?? '',
      TotalPressureAfterRisingClose: r.totalPressureAfterRisingClose ?? '',
      PressureAfterDoorOpen: r.pressureAfterDoorOpen ?? '',
      Plant1BatchCount: r.plant1BatchCount ?? '',
      Plant2BatchCount: r.plant2BatchCount ?? '',
      Remarks: r.remarks ?? ''
    }));

    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Autoclave');
    const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const blob = new Blob([buffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `Autoclave_Report_${this.filterFromDate || 'all'}_to_${this.filterToDate || 'all'}.xlsx`;
    a.click();
    URL.revokeObjectURL(url);
  }




  exportPdf(): void {
    if (!this.filterFromDate || !this.filterToDate) {
      alert('Please select date range');
      return;
    }
    this.workflowService.exportReport('AUTOCLAVE', this.filterFromDate, this.filterToDate, 'pdf').subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Autoclave_Report_${this.filterFromDate}_to_${this.filterToDate}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => alert('Failed to export PDF')
    });
  }

  downloadAutoclave(r: any, format: string = 'pdf') {
    if (!r) return;
    let batchNo = '';
    if (r.batchNo) {
      batchNo = String(r.batchNo).split(',')[0].trim();
    }
    if (!batchNo && r.wagons && r.wagons.length > 0) {
      batchNo = r.wagons[0].mBatch || r.wagons[0].eBatch || r.wagons[0].wBatch || '';
    }
    if (!batchNo) { alert('No batch number found in this autoclave cycle.'); return; }
    this.workflowService.downloadReport(batchNo, 'AUTOCLAVE', format).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const ext = format === 'excel' ? 'xlsx' : 'pdf';
        a.download = `workflow_report_${batchNo}_AUTOCLAVE.${ext}`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { alert('Failed to download report.'); }
    });
  }

  download(format: string): void {
    if (this.selected) {
      this.downloadAutoclave(this.selected, format);
    }
  }

  canViewAutoclave(r: any): boolean {
    return true;
  }

  openAutoclaveModal(r: any): void {
    console.log('Opening details for:', r);
  }

  /** Download combined horizontal Excel for one batch from this autoclave cycle */
  downloadHorizontalReport(r: any) {
    let batchNo = '';
    if (r.batchNo) {
      batchNo = String(r.batchNo).split(',')[0].trim();
    }
    if (!batchNo && r?.wagons?.length > 0) {
      batchNo = r.wagons[0].mBatch || r.wagons[0].eBatch || r.wagons[0].wBatch || '';
    }
    if (!batchNo) { alert('No batch number found.'); return; }
    this.horizontalReportService.downloadLifecycleExcel(batchNo, 'AUTOCLAVE').subscribe({
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
    this.horizontalReportService.downloadExcel(this.filterFromDate, this.filterToDate, undefined, 'AUTOCLAVE', this.filterPlant, this.filterShift).subscribe({
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

  // ================= IMPORT =================
  onImportSelect(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;

    if (value === 'excel') {
      const fileInput = document.getElementById('autoclaveExcelInput') as HTMLInputElement;
      fileInput?.click();
    }

    (event.target as HTMLSelectElement).value = '';
  }

  onExcelSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    const reader = new FileReader();

    reader.onload = (e: any) => {
      const wb = XLSX.read(e.target.result, { type: 'binary' });
      const ws = wb.Sheets[wb.SheetNames[0]];
      const data = XLSX.utils.sheet_to_json(ws);

      console.log('Imported Excel Data:', data);

      // 🔥 OPTIONAL: call backend API here
      // this.service.bulkImport(data).subscribe(...)
    };

    reader.readAsBinaryString(file);
  }

  getUsedBatches(): string[] {
    const used: string[] = [];
    this.wagons.controls.forEach(ctrl => {
      const v = ctrl.value;
      if (v.mBatch) used.push(v.mBatch);
      if (v.eBatch) used.push(v.eBatch);
      if (v.wBatch) used.push(v.wBatch);
    });
    return used;
  }

  canAddWagon(): boolean {
    const v = this.wagonForm.value;
    return !!(v.mBatch || v.eBatch || v.wBatch);  // at least one batch is required
  }


}
