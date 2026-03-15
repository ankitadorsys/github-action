import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { TaskService } from '../../core/api/task.service';
import { Task } from '../../shared/models/task.model';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './task-detail.component.html',
  styleUrl: './task-detail.component.css',
})
export class TaskDetailComponent implements OnInit {
  task?: Task;
  errorMessage = '';
  loading = true;
  deleting = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly taskService: TaskService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!Number.isFinite(id)) {
      this.errorMessage = 'Invalid task id.';
      return;
    }

    this.taskService.getTaskById(id).subscribe({
      next: (task) => {
        this.task = task;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load task details.';
        this.loading = false;
      },
    });
  }

  deleteTask(): void {
    if (!this.task || this.deleting) {
      return;
    }

    const confirmed = window.confirm(`Delete task "${this.task.title}"?`);
    if (!confirmed) {
      return;
    }

    this.deleting = true;
    this.taskService.deleteTask(this.task.id).subscribe({
      next: () => {
        this.deleting = false;
        void this.router.navigate(['/tasks']);
      },
      error: () => {
        this.errorMessage = 'Task could not be deleted.';
        this.deleting = false;
      },
    });
  }
}
