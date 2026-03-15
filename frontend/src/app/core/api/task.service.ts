import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Task } from '../../shared/models/task.model';

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly tasksUrl = `${environment.apiBaseUrl}/tasks`;

  constructor(private readonly httpClient: HttpClient) {}

  getAllTasks(): Observable<Task[]> {
    return this.httpClient.get<Task[]>(this.tasksUrl);
  }

  getTaskById(id: number): Observable<Task> {
    return this.httpClient.get<Task>(`${this.tasksUrl}/${id}`);
  }
}
