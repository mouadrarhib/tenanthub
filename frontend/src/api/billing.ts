import { apiFetch } from './client';
import type { UsageSummary } from '../types';

export function getUsage(): Promise<UsageSummary> {
  return apiFetch<UsageSummary>('/api/billing/usage');
}
