import { useState, type FormEvent } from 'react';
import { createTask } from '../api/tasks';
import { getMe } from '../api/me';
import type { Task, TaskStatus } from '../types';

export function CreateTaskForm({ projectId, onCreated }: { projectId: string; onCreated: (task: Task) => void }) {
  const [title, setTitle] = useState('');
  const [status, setStatus] = useState<TaskStatus>('TODO');
  const [dueDate, setDueDate] = useState('');
  const [assigneeUserId, setAssigneeUserId] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isFetchingMe, setIsFetchingMe] = useState(false);

  const handleAssignToMe = async () => {
    setIsFetchingMe(true);
    try {
      const { userId } = await getMe();
      setAssigneeUserId(userId);
    } catch {
      setError('Failed to look up your user id');
    } finally {
      setIsFetchingMe(false);
    }
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      const task = await createTask(projectId, {
        title,
        status,
        assigneeUserId: assigneeUserId.trim() || null,
        dueDate: dueDate || null,
      });
      onCreated(task);
      setTitle('');
      setDueDate('');
      setStatus('TODO');
      setAssigneeUserId('');
    } catch {
      setError('Failed to create task');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="mt-4 flex flex-wrap items-end gap-3">
      <div className="min-w-[160px] flex-1">
        <label htmlFor="title" className="block text-xs font-medium text-slate-500">
          Title
        </label>
        <input
          id="title"
          required
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
        />
      </div>

      <div>
        <label htmlFor="status" className="block text-xs font-medium text-slate-500">
          Status
        </label>
        <select
          id="status"
          value={status}
          onChange={(e) => setStatus(e.target.value as TaskStatus)}
          className="mt-1 rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
        >
          <option value="TODO">TODO</option>
          <option value="DOING">DOING</option>
          <option value="DONE">DONE</option>
        </select>
      </div>

      <div>
        <label htmlFor="dueDate" className="block text-xs font-medium text-slate-500">
          Due date
        </label>
        <input
          id="dueDate"
          type="date"
          value={dueDate}
          onChange={(e) => setDueDate(e.target.value)}
          className="mt-1 rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
        />
      </div>

      <div className="min-w-[220px] flex-1">
        <label htmlFor="assigneeUserId" className="block text-xs font-medium text-slate-500">
          Assignee user ID (optional)
        </label>
        <div className="mt-1 flex gap-2">
          <input
            id="assigneeUserId"
            value={assigneeUserId}
            onChange={(e) => setAssigneeUserId(e.target.value)}
            placeholder="paste a user's id to notify them by email"
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none focus:ring-1 focus:ring-slate-500"
          />
          <button
            type="button"
            onClick={handleAssignToMe}
            disabled={isFetchingMe}
            className="whitespace-nowrap rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-600 transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {isFetchingMe ? 'Loading…' : 'Assign to me'}
          </button>
        </div>
      </div>

      <button
        type="submit"
        disabled={isSubmitting}
        className="rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {isSubmitting ? 'Adding…' : 'Add task'}
      </button>

      {error && <p className="w-full text-sm text-red-600">{error}</p>}
    </form>
  );
}
