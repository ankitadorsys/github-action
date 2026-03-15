import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { TaskService } from '../../core/api/task.service';
import { Task } from '../../shared/models/task.model';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './task-detail.component.html',
  styleUrl: './task-detail.component.css',
})
export class TaskDetailComponent implements OnInit {
  task?: Task;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly taskService: TaskService,
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
      },
      error: () => {
        this.errorMessage = 'Unable to load task details.';
      },
    });
  }
}
