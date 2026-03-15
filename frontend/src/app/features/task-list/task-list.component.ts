import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { TaskService } from '../../core/api/task.service';
import { Task, TaskPriority, TaskStatus } from '../../shared/models/task.model';

@Component({
  selector: 'app-task-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './task-list.component.html',
  styleUrl: './task-list.component.css',
})
export class TaskListComponent implements OnInit {
  tasks: Task[] = [];
  errorMessage = '';
  loading = true;

  constructor(private readonly taskService: TaskService) {}

  ngOnInit(): void {
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
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
