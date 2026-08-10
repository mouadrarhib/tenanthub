export interface AuthResponse {
  token: string;
}

export interface JwtClaims {
  sub: string;
  email: string;
  tenantId: string;
  roles: string[];
  iat: number;
  exp: number;
}

export interface Project {
  id: string;
  tenantId: string;
  name: string;
  description: string | null;
  createdAt: string;
}

export type TaskStatus = 'TODO' | 'DOING' | 'DONE';

export interface Task {
  id: string;
  projectId: string;
  title: string;
  status: TaskStatus;
  assigneeUserId: string | null;
  dueDate: string | null;
}

export interface TaskRequest {
  title: string;
  status: TaskStatus;
  assigneeUserId: string | null;
  dueDate: string | null;
}

export interface MeResponse {
  userId: string;
}

export interface UsageSummary {
  tenantId: string;
  planName: string;
  maxUsers: number;
  maxProjects: number;
  projectsUsed: number;
}
