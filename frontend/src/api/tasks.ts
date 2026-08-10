import { apiFetch } from './client';
import type { Task, TaskRequest } from '../types';

export function listTasks(projectId: string): Promise<Task[]> {
  return apiFetch<Task[]>(`/api/projects/${projectId}/tasks`);
}

export function createTask(projectId: string, request: TaskRequest): Promise<Task> {
  return apiFetch<Task>(`/api/projects/${projectId}/tasks`, {
    method: 'POST',
    body: JSON.stringify(request),
  });
}
