import { apiFetch } from './client';
import type { Project } from '../types';

export function listProjects(): Promise<Project[]> {
  return apiFetch<Project[]>('/api/projects');
}
