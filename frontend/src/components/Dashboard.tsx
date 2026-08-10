import { useEffect, useState } from 'react';
import { listProjects } from '../api/projects';
import { listTasks } from '../api/tasks';
import { AppShell } from './AppShell';
import { TaskList } from './TaskList';
import { CreateTaskForm } from './CreateTaskForm';
import { UsageWidget } from './UsageWidget';
import type { Project, Task } from '../types';

export function Dashboard() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [tasks, setTasks] = useState<Task[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    listProjects()
      .then((fetched) => {
        setProjects(fetched);
        setSelectedProjectId(fetched[0]?.id ?? null);
      })
      .finally(() => setIsLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedProjectId) {
      setTasks([]);
      return;
    }
    listTasks(selectedProjectId).then(setTasks);
  }, [selectedProjectId]);

  if (isLoading) {
    return (
      <AppShell>
        <p className="text-sm text-slate-400">Loading…</p>
      </AppShell>
    );
  }

  if (projects.length === 0) {
    return (
      <AppShell>
        <p className="text-sm text-slate-400">No projects yet — create one from the API to get started.</p>
      </AppShell>
    );
  }

  return (
    <AppShell>
      <div className="grid gap-6 md:grid-cols-[2fr_1fr]">
        <section>
          <div className="flex items-center justify-between">
            <label htmlFor="project" className="text-xs font-medium text-slate-500">
              Project
            </label>
            <select
              id="project"
              value={selectedProjectId ?? ''}
              onChange={(e) => setSelectedProjectId(e.target.value)}
              className="rounded-md border border-slate-300 px-2 py-1 text-sm"
            >
              {projects.map((project) => (
                <option key={project.id} value={project.id}>
                  {project.name}
                </option>
              ))}
            </select>
          </div>

          <div className="mt-3">
            <TaskList tasks={tasks} />
          </div>

          {selectedProjectId && (
            <CreateTaskForm
              projectId={selectedProjectId}
              onCreated={(task) => setTasks((prev) => [...prev, task])}
            />
          )}
        </section>

        <aside>
          <UsageWidget />
        </aside>
      </div>
    </AppShell>
  );
}
