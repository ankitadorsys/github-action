import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './task-form.component.html',
  styleUrl: './task-form.component.css',
})
export class TaskFormComponent {
  readonly taskForm = this.formBuilder.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: [''],
  });

  constructor(private readonly formBuilder: FormBuilder) {}

  onSubmit(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }
  }
}
