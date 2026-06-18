import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BatchLookupService } from '../services/batch-lookup.service';

@Component({
  selector: 'app-batch-history',
  templateUrl: './batch-history.component.html',
  styleUrls: ['./batch-history.component.css']
})
export class BatchHistoryComponent implements OnInit {

  searchBatchNo = '';
  loading = false;
  error = '';
  traceability: any = null;

  stages = [
    { key: 'production',  label: 'Production',      icon: '🏭', color: '#2e7d32', fields: ['batchNo','shift','plantName','tempC','cementKg','limeKg','gypsumKg','aiPowerGm','waterLiter','totalSolid'] },
    { key: 'casting',     label: 'Casting',          icon: '🪣', color: '#b71c1c', fields: ['batchNo','shift','plantName','height','castingTempC','mouldNo','mouldHeight','mouldFlow','remark'] },
    { key: 'rising',      label: 'Rising Section',   icon: '📈', color: '#1565c0', fields: ['batchNo','shift','plantName','risingTempC','mouldHeight','mouldFlow','risingTime','remark'] },
    { key: 'cutting',     label: 'Wire Cutting',     icon: '✂️',  color: '#6a1b9a', fields: ['batchNo','shift','plantName','cuttingDate','ballTestMm','cuttingTempC','cuttingHours','totalItem','time','remark'] },
    { key: 'autoclave',   label: 'Autoclave',        icon: '🔥', color: '#e65100', fields: ['batchNo','shift','plantName','autoclaveNo','runNo','holdingHours','pressureAchieved','temperatureAchieved','cycleCompletedTime'] },
    { key: 'separating',  label: 'Block Separating', icon: '🧱', color: '#4a148c', fields: ['batchNumber','shift','plantName','blockSize','startTime','endTime','duration','totalPcs','totalBreakage','breakagePercent'] },
    { key: 'cubeTest',    label: 'Cube Test',        icon: '🧪', color: '#00695c', fields: ['batchNo','shift','plantName','castDate','testingDate','demouldDensity','wetDensity','wetStrength','dryDensity','dryStrength'] },
    { key: 'rejection',   label: 'Rejection',        icon: '🚫', color: '#ad1457', fields: ['batchNo','shift','plantName','blockSize','qty','totalBreakages','totalRejection','crackRejection','dimensionFailure','strengthFailure'] }
  ];

  fieldLabels: Record<string, string> = {
    batchNo: 'Batch No', batchNumber: 'Batch No',
    shift: 'Shift', plantName: 'Plant',
    tempC: 'Temp (°C)', cementKg: 'Cement (kg)', limeKg: 'Lime (kg)',
    gypsumKg: 'Gypsum (kg)', aiPowerGm: 'AI Powder (g)',
    waterLiter: 'Water (L)', totalSolid: 'Total Solid',
    height: 'Height', castingTempC: 'Rising Temp (°C)',
    mouldNo: 'Mould No', mouldHeight: 'Mould Height', mouldFlow: 'Mould Flow',
    risingTempC: 'Rising Temp (°C)', risingTime: 'Rising Time',
    cuttingDate: 'Cutting Date', ballTestMm: 'Ball Test (mm)',
    cuttingTempC: 'Cutting Temp (°C)', cuttingHours: 'Cutting Hours',
    totalItem: 'Total Items', time: 'Time',
    autoclaveNo: 'Autoclave No', runNo: 'Run No',
    holdingHours: 'Holding Hours', pressureAchieved: 'Pressure (bar)',
    temperatureAchieved: 'Temp Achieved (°C)', cycleCompletedTime: 'Completed At',
    blockSize: 'Block Size', startTime: 'Start Time', endTime: 'End Time',
    duration: 'Duration', totalPcs: 'Total Pcs',
    totalBreakage: 'Total Breakage', breakagePercent: 'Breakage %',
    castDate: 'Cast Date', testingDate: 'Test Date',
    demouldDensity: 'Demould Density', wetDensity: 'Wet Density',
    wetStrength: 'Wet Strength', dryDensity: 'Dry Density', dryStrength: 'Dry Strength',
    qty: 'Qty', totalBreakages: 'Total Breakages', totalRejection: 'Total Rejection',
    crackRejection: 'Crack Rejection', dimensionFailure: 'Dimension Fail',
    strengthFailure: 'Strength Fail', remark: 'Remark', remarks: 'Remark'
  };

  constructor(
    private batchLookup: BatchLookupService,
    private router: Router
  ) {}

  ngOnInit(): void {}

  search(): void {
    const batchNo = this.searchBatchNo.trim();
    if (!batchNo) return;
    this.loading = true;
    this.error = '';
    this.traceability = null;

    this.batchLookup.getBatchTraceability(batchNo).subscribe({
      next: (res) => {
        this.traceability = res;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'No data found for batch: ' + batchNo;
        this.loading = false;
      }
    });
  }

  clear(): void {
    this.searchBatchNo = '';
    this.traceability = null;
    this.error = '';
  }

  getStageData(stage: any): any {
    return this.traceability ? this.traceability[stage.key] : null;
  }

  getFieldLabel(key: string): string {
    return this.fieldLabels[key] || key;
  }

  formatValue(val: any): string {
    if (val === null || val === undefined || val === '') return '—';
    if (typeof val === 'boolean') return val ? 'Yes' : 'No';
    if (typeof val === 'number') return String(val);
    // Date detection
    if (typeof val === 'string' && val.includes('T') && val.includes('-')) {
      try {
        return new Date(val).toLocaleDateString('en-GB');
      } catch { return val; }
    }
    return String(val);
  }

  goToDashboard(): void {
    this.router.navigate(['/production-dashboard']);
  }
}
