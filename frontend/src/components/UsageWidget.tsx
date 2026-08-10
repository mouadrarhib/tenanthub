import { useEffect, useState } from 'react';
import { getUsage } from '../api/billing';
import { ApiError } from '../api/client';
import type { UsageSummary } from '../types';

export function UsageWidget() {
  const [usage, setUsage] = useState<UsageSummary | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getUsage()
      .then(setUsage)
      .catch((err) => {
        setError(err instanceof ApiError && err.status === 404 ? 'No usage data yet' : 'Failed to load usage');
      });
  }, []);

  if (error) {
    return <p className="text-sm text-slate-400">{error}</p>;
  }

  if (!usage) {
    return <p className="text-sm text-slate-400">Loading usage…</p>;
  }

  const projectsPct = Math.min(100, (usage.projectsUsed / usage.maxProjects) * 100);

  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5 shadow-sm">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold text-slate-900">Plan usage</h2>
        <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
          {usage.planName}
        </span>
      </div>

      <div className="mt-4">
        <div className="flex justify-between text-xs text-slate-500">
          <span>Projects</span>
          <span>
            {usage.projectsUsed} / {usage.maxProjects}
          </span>
        </div>
        <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-slate-100">
          <div
            className={`h-full rounded-full ${projectsPct >= 100 ? 'bg-red-500' : 'bg-slate-900'}`}
            style={{ width: `${projectsPct}%` }}
          />
        </div>
      </div>

      <p className="mt-3 text-xs text-slate-400">Up to {usage.maxUsers} users on this plan</p>
    </div>
  );
}
