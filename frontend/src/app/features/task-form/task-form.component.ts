import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { TaskPriority, TaskStatus } from '../../shared/models/task.model';

import { TaskService } from '../../core/api/task.service';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-form.component.html',
  styleUrl: './task-form.component.css',
})
export class TaskFormComponent {
  loading = false;
  errorMessage = '';
  isEditMode = false;
  taskId?: number;

  readonly statusOptions: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
  readonly priorityOptions: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

  readonly taskForm = this.formBuilder.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
    status: ['TODO' as TaskStatus],
    priority: ['MEDIUM' as TaskPriority],
    dueDate: [''],
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly taskService: TaskService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam === null ? Number.NaN : Number(idParam);
    if (Number.isInteger(id) && id > 0) {
      this.isEditMode = true;
      this.taskId = id;
      this.loadTaskForEdit(id);
    }
  }

  onSubmit(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const formValue = this.taskForm.getRawValue();

    const request = {
      title: formValue.title?.trim() ?? '',
      description: formValue.description?.trim() || undefined,
      status: formValue.status ?? 'TODO',
      priority: formValue.priority ?? 'MEDIUM',
      dueDate: formValue.dueDate || undefined,
    };

    const saveOperation = this.isEditMode && this.taskId
      ? this.taskService.updateTask(this.taskId, request)
      : this.taskService.createTask(request);

    saveOperation.subscribe({
      next: (task) => {
        this.loading = false;
        void this.router.navigate(['/tasks', task.id]);
      },
      error: () => {
        this.loading = false;
        this.errorMessage = this.isEditMode
          ? 'Task could not be updated. Please verify your session and input.'
          : 'Task could not be created. Please verify your session and input.';
      },
    });
  }

  private loadTaskForEdit(id: number): void {
    this.loading = true;
    this.taskService.getTaskById(id).subscribe({
      next: (task) => {
        this.taskForm.patchValue({
          title: task.title,
          description: task.description ?? '',
          status: task.status,
          priority: task.priority,
          dueDate: task.dueDate ?? '',
        });
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load task for editing.';
        this.loading = false;
      },
    });
  }
}
