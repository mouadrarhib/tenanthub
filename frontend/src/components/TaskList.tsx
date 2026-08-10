import type { Task } from '../types';

const STATUS_STYLES: Record<Task['status'], string> = {
  TODO: 'bg-slate-100 text-slate-600',
  DOING: 'bg-amber-100 text-amber-700',
  DONE: 'bg-emerald-100 text-emerald-700',
};

export function TaskList({ tasks }: { tasks: Task[] }) {
  if (tasks.length === 0) {
    return <p className="text-sm text-slate-400">No tasks yet — create the first one below.</p>;
  }

  return (
    <ul className="divide-y divide-slate-200 rounded-xl border border-slate-200 bg-white shadow-sm">
      {tasks.map((task) => (
        <li key={task.id} className="flex items-center justify-between px-4 py-3">
          <div>
            <p className="text-sm font-medium text-slate-900">{task.title}</p>
            {task.dueDate && <p className="text-xs text-slate-400">Due {task.dueDate}</p>}
          </div>
          <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_STYLES[task.status]}`}>
            {task.status}
          </span>
        </li>
      ))}
    </ul>
  );
}
