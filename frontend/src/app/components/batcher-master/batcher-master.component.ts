import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BatcherService } from 'src/app/services/batcher.service';

@Component({
  selector: 'app-batcher-master',
  templateUrl: './batcher-master.component.html',
  styleUrls: ['./batcher-master.component.css']
})
export class BatcherMasterComponent implements OnInit {

  form!: FormGroup;
  batchers: any[] = [];
  paginatedBatchers: any[] = [];

  isEdit: boolean = false;
  editId: number | null = null;

  page = 1;
  pageSize = 5;

  constructor(
    private fb: FormBuilder,
    private batcherService: BatcherService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', Validators.required]
    });
    this.loadBatchers();
  }

  loadBatchers(): void {
    this.batcherService.getAllBatchers().subscribe({
      next: (data) => {
        this.batchers = data.sort((a: any, b: any) => b.id - a.id);
        this.setPage(1);
      },
      error: () => {
        alert('Failed to load batchers list');
      }
    });
  }

  prevPage(): void {
    if (this.page > 1) {
      this.setPage(this.page - 1);
    }
  }

  nextPage(): void {
    if (this.page < this.totalPages()) {
      this.setPage(this.page + 1);
    }
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: any = {
      name: this.form.value.name.trim()
    };

    if (this.isEdit && this.editId !== null) {
      payload.id = this.editId;
      this.batcherService.updateBatcher(this.editId, payload).subscribe({
        next: () => {
          alert('Batcher updated successfully');
          this.resetForm();
          this.loadBatchers();
        },
        error: (err) => {
          alert(err?.error?.message || 'Error updating batcher');
        }
      });
    } else {
      this.batcherService.createBatcher(payload).subscribe({
        next: () => {
          alert('Batcher added successfully');
          this.resetForm();
          this.loadBatchers();
        },
        error: (err) => {
          alert(err?.error?.message || 'Error saving batcher');
        }
      });
    }
  }

  editBatcher(batcher: any): void {
    this.isEdit = true;
    this.editId = batcher.id;

    this.form.patchValue({
      name: batcher.name
    });
  }

  deleteBatcher(batcher: any): void {
    if(confirm("Are you sure you want to delete this batcher?")) {
      this.batcherService.deleteBatcher(batcher.id).subscribe({
        next: () => {
          alert('Batcher deleted successfully');
          this.loadBatchers();
        },
        error: (err) => {
          alert('Error deleting batcher');
        }
      });
    }
  }

  resetForm(): void {
    this.form.reset();
    this.isEdit = false;
    this.editId = null;
  }

  setPage(page: number): void {
    this.page = page;
    const start = (page - 1) * this.pageSize;
    const end = start + this.pageSize;
    this.paginatedBatchers = this.batchers.slice(start, end);
  }

  totalPages(): number {
    return Math.ceil(this.batchers.length / this.pageSize);
  }
}
