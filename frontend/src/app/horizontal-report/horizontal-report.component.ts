import { Component, OnInit } from '@angular/core';
import { HorizontalReportService, HorizontalReportRow } from '../services/horizontal-report.service';

interface StageConfig {
    key: keyof HorizontalReportRow;
    label: string;
    theme: string;
    columns: { key: string; label: string }[];
}

@Component({
    selector: 'app-horizontal-report',
    templateUrl: './horizontal-report.component.html',
    styleUrls: ['./horizontal-report.component.css']
})
export class HorizontalReportComponent implements OnInit {

    // ─── Filters ───────────────────────────────────────────────────────────────
    fromDate = '';
    toDate = '';
    batchNo = '';
    plantFilter = 'Plant 1';
    shiftFilter = '';
    shifts: string[] = [
        'Night (00:00 - 08:00) [1st Shift]',
        'Morning (08:00 - 16:00) [2nd Shift]',
        'Afternoon (16:00 - 00:00) [3rd Shift]'
    ];

    // ─── Data ──────────────────────────────────────────────────────────────────
    allRows: HorizontalReportRow[] = [];
    pagedRows: HorizontalReportRow[] = [];
    loading = false;
    error = '';

    // ─── Pagination ────────────────────────────────────────────────────────────
    currentPage = 1;
    pageSize = 20;
    totalPages = 1;

    // ─── Stage definitions (order + columns) ──────────────────────────────────
    stages: StageConfig[] = [
        {
            key: 'production', label: 'Production', theme: 'production',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'siloNo1', label: 'Silo No.' },
                { key: 'faSolid1', label: 'Fa Solid' },
                { key: 'totalSolid', label: 'Total Solid' },
                { key: 'faSlurryQty', label: 'FA Slurry Qty' },
                { key: 'excessSlurryQty', label: 'Excess Slurry Qty' },
                { key: 'waterLiter', label: 'Water Lite' },
                { key: 'cementKg', label: 'Cement Kg' },
                { key: 'limeKg', label: 'Lime Kg' },
                { key: 'gypsumKg', label: 'Gypsum K' },
                { key: 'solOilKg', label: 'Dol Oil K' },
                { key: 'surfactant', label: 'Surfactant' },
                { key: 'aluminumPowderKg', label: 'Al Powder' },
                { key: 'dcmrt', label: 'DC MRT' },
                { key: 'mixingTime', label: 'Mixing Time' },
                { key: 'tempC', label: 'CnTemp' },
                { key: 'productionTime', label: 'Production Tim' },
                { key: 'remark', label: 'Remark' },
            ]
        },
        {
            key: 'casting', label: 'Casting', theme: 'casting',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'mouldNo', label: 'Mould No' },
                { key: 'flowInCm', label: 'Flow In C' },
                { key: 'tempC', label: 'CnTemp C' },
                { key: 'remark', label: 'Remark' },
            ]
        },
        {
            key: 'cutting', label: 'Wire Cutting', theme: 'cutting',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'cuttingDate', label: 'Cutting Date' },
                { key: 'mouldNo', label: 'Mould No' },
                { key: 'size', label: 'Size' },
                { key: 'ballTestMm', label: 'Ball Test M' },
                { key: 'totalItem', label: 'Total Item' },
                { key: 'cuttingTempC', label: 'Cutting Temp' },
                { key: 'cuttingHours', label: 'Cutting Hours' },
                { key: 'time', label: 'Time' },
                { key: 'remark', label: 'Remark' },
            ]
        },
        {
            key: 'autoclave', label: 'Autoclave', theme: 'autoclave',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'autoclaveNo', label: 'Auto No' },
                { key: 'runNo', label: 'Run No' },
                { key: 'currentStatus', label: 'Status' },
                { key: 'startedAt', label: 'Started At' },
                { key: 'completedAt', label: 'Completed At' },
                { key: 'remarks', label: 'Remarks' },
            ]
        },
        {
            key: 'blockSeparating', label: 'Block Separating', theme: 'block',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'batchNumber', label: 'Batch No' },
                { key: 'blockSize', label: 'Block Size' },
                { key: 'time', label: 'Time' },
            ]
        },
        {
            key: 'cubeTest', label: 'Cube Test', theme: 'cube',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'castDate', label: 'Cast Date' },
                { key: 'testingDate', label: 'Test Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'cubeDimensionImmediate', label: 'Dimension' },
                { key: 'densityKgM3', label: 'Density (kg/m³)' },
            ]
        },
        {
            key: 'rejection', label: 'Rejection', theme: 'rejection',
            columns: [
                { key: 'date', label: 'Date' },
                { key: 'shift', label: 'Shift' },
                { key: 'blockSize', label: 'Block Size' },
                { key: 'qty', label: 'Qty' },
                { key: 'totalBreakages', label: 'Breakages' },
                { key: 'remarks', label: 'Remarks' },
            ]
        }
    ];

    constructor(private reportService: HorizontalReportService) { }

    ngOnInit(): void {
        this.loadReport();
    }

    loadReport(): void {
        this.loading = true;
        this.error = '';
        this.reportService.getReport(
            this.fromDate || undefined,
            this.toDate || undefined,
            this.batchNo || undefined,
            this.plantFilter || undefined,
            this.shiftFilter || undefined
        ).subscribe({
            next: (data) => {
                this.allRows = data;
                this.currentPage = 1;
                this.computePagination();
                this.loading = false;
            },
            error: () => {
                this.error = 'Failed to load report. Please try again.';
                this.loading = false;
            }
        });
    }

    clearFilters(): void {
        this.fromDate = '';
        this.toDate = '';
        this.batchNo = '';
        this.plantFilter = 'Plant 1';
        this.shiftFilter = '';
        this.loadReport();
    }

    computePagination(): void {
        this.totalPages = Math.max(1, Math.ceil(this.allRows.length / this.pageSize));
        this.updatePage();
    }

    updatePage(): void {
        const start = (this.currentPage - 1) * this.pageSize;
        this.pagedRows = this.allRows.slice(start, start + this.pageSize);
    }

    prevPage(): void {
        if (this.currentPage > 1) { this.currentPage--; this.updatePage(); }
    }

    nextPage(): void {
        if (this.currentPage < this.totalPages) { this.currentPage++; this.updatePage(); }
    }

    goToPage(p: number): void {
        this.currentPage = p;
        this.updatePage();
    }

    get pageNumbers(): number[] {
        return Array.from({ length: this.totalPages }, (_, i) => i + 1);
    }

    /** Safe field lookup from a stage data map */
    field(row: HorizontalReportRow, stageKey: keyof HorizontalReportRow, col: string): string {
        const stage = row[stageKey] as { [key: string]: string };
        return (stage && stage[col]) ? stage[col] : '—';
    }

    /** Whether a row has any data for a stage */
    hasData(row: HorizontalReportRow, stageKey: keyof HorizontalReportRow): boolean {
        const stage = row[stageKey] as { [key: string]: string };
        return stage && Object.keys(stage).length > 0;
    }

    get totalColumns(): number {
        return 1 + this.stages.reduce((sum, s) => sum + s.columns.length, 0);
    }
}
