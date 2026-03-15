import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { CreateTaskRequest, Task, TaskPriority, TaskStatus } from '../../shared/models/task.model';

export interface TaskQueryParams {
  status?: TaskStatus;
  priority?: TaskPriority;
  dueDateFrom?: string;
  dueDateTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({
  providedIn: 'root',
})
export class TaskService {
  private readonly tasksUrl = `${environment.apiBaseUrl}/tasks`;

  constructor(private readonly httpClient: HttpClient) {}

  getAllTasks(params: TaskQueryParams): Observable<HttpResponse<Task[]>> {
    let httpParams = new HttpParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });

    return this.httpClient.get<Task[]>(this.tasksUrl, {
      params: httpParams,
      observe: 'response',
    });
  }

  getTaskById(id: number): Observable<Task> {
    return this.httpClient.get<Task>(`${this.tasksUrl}/${id}`);
  }

  createTask(request: CreateTaskRequest): Observable<Task> {
    return this.httpClient.post<Task>(this.tasksUrl, request);
  }

  updateTask(id: number, request: CreateTaskRequest): Observable<Task> {
    return this.httpClient.put<Task>(`${this.tasksUrl}/${id}`, request);
  }

  deleteTask(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.tasksUrl}/${id}`);
  }
}
