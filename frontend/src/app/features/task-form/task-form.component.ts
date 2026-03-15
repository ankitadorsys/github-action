import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

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

  readonly taskForm = this.formBuilder.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
  });

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly taskService: TaskService,
    private readonly router: Router,
  ) {}

  onSubmit(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const formValue = this.taskForm.getRawValue();

    this.taskService
      .createTask({
        title: formValue.title?.trim() ?? '',
        description: formValue.description?.trim() || undefined,
        status: 'TODO',
        priority: 'MEDIUM',
      })
      .subscribe({
        next: (task) => {
          this.loading = false;
          void this.router.navigate(['/tasks', task.id]);
        },
        error: () => {
          this.loading = false;
          this.errorMessage =
            'Task could not be created. Please verify your session and input.';
        },
      });
  }
}
