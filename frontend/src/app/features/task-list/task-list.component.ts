import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { TaskService } from '../../core/api/task.service';
import { Task, TaskPriority, TaskStatus } from '../../shared/models/task.model';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css',
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  errorMessage = '';
  loading = true;
  totalCount = 0;
  totalPages = 0;
  page = 0;
  size = 10;

  statusFilter = '';
  priorityFilter = '';
  dueDateFrom = '';
  dueDateTo = '';

  readonly statusOptions: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
  readonly priorityOptions: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

  constructor(private readonly taskService: TaskService) {}

  ngOnInit(): void {
    this.loadTasks();
  }

  applyFilters(): void {
    this.page = 0;
    this.loadTasks();
  }

  resetFilters(): void {
    this.statusFilter = '';
    this.priorityFilter = '';
    this.dueDateFrom = '';
    this.dueDateTo = '';
    this.page = 0;
    this.loadTasks();
  }

  previousPage(): void {
    if (this.page === 0 || this.loading) {
      return;
    }
    this.page -= 1;
    this.loadTasks();
  }

  nextPage(): void {
    if (this.loading || this.page + 1 >= this.totalPages) {
      return;
    }
    this.page += 1;
    this.loadTasks();
  }

  private loadTasks(): void {
    this.loading = true;
    this.errorMessage = '';

    this.taskService.getAllTasks({
      status: (this.statusFilter || undefined) as TaskStatus | undefined,
      priority: (this.priorityFilter || undefined) as TaskPriority | undefined,
      dueDateFrom: this.dueDateFrom || undefined,
      dueDateTo: this.dueDateTo || undefined,
      page: this.page,
      size: this.size,
      sort: 'updatedAt,desc',
    }).subscribe({
      next: (tasks) => {
        this.tasks = tasks.body ?? [];
        this.totalCount = Number(tasks.headers.get('X-Total-Count') ?? this.tasks.length);
        this.totalPages = Number(tasks.headers.get('X-Total-Pages') ?? 1);
        this.page = Number(tasks.headers.get('X-Page') ?? this.page);
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Unable to load tasks. Verify backend and authentication.';
        this.loading = false;
      },
    });
  }

  statusClass(status: TaskStatus): string {
    if (status === 'DONE') {
      return 'done';
    }
    if (status === 'IN_PROGRESS') {
      return 'in-progress';
    }
    return 'todo';
  }

  priorityClass(priority: TaskPriority): string {
    if (priority === 'HIGH') {
      return 'high';
    }
    if (priority === 'MEDIUM') {
      return 'medium';
    }
    return 'low';
  }
}
