"use client";

import { FormEvent, useCallback, useEffect, useState } from "react";

type Project = { id: string; name: string; repositoryUrl: string; repositoryFullName: string; branch: string };
type Pipeline = { id: string; projectId: string; projectName: string; commitSha: string | null; branch: string; status: string; triggerType: string; createdAt: string };

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api/v1${path}`, {
    ...init,
    headers: { "Content-Type": "application/json", ...init?.headers },
    cache: "no-store",
  });
  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new Error(body?.error?.message ?? `Request failed (${response.status})`);
  }
  return response.status === 204 ? undefined as T : response.json();
}

export default function Home() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [pipelines, setPipelines] = useState<Pipeline[]>([]);
  const [name, setName] = useState("");
  const [repositoryUrl, setRepositoryUrl] = useState("");
  const [branch, setBranch] = useState("main");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const load = useCallback(async (signal?: AbortSignal) => {
    const [projectData, pipelineData] = await Promise.all([
      api<{projects: Project[]}>("/projects", { signal }),
      api<{pipelines: Pipeline[]}>("/pipelines", { signal }),
    ]);
    setProjects(projectData.projects);
    setPipelines(pipelineData.pipelines);
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    load(controller.signal).catch(e => {
      if (!controller.signal.aborted) setError(e.message);
    }).finally(() => {
      if (!controller.signal.aborted) setLoading(false);
    });
    return () => controller.abort();
  }, [load]);

  async function createProject(event: FormEvent) {
    event.preventDefault();
    if (busy) return;
    setBusy(true); setError("");
    try {
      const data = await api<{project: Project}>("/projects", {
        method: "POST", body: JSON.stringify({ name, repositoryUrl, branch }),
      });
      setProjects(previous => [data.project, ...previous]);
      setName(""); setRepositoryUrl(""); setBranch("main");
    } catch (e) { setError(e instanceof Error ? e.message : "Could not create project"); }
    finally { setBusy(false); }
  }

  async function trigger(projectId: string) {
    if (busy) return;
    setBusy(true); setError("");
    try {
      const data = await api<{pipeline: Pipeline}>(`/projects/${projectId}/pipelines`, { method: "POST" });
      setPipelines(previous => [data.pipeline, ...previous].slice(0, 50));
    } catch (e) { setError(e instanceof Error ? e.message : "Could not queue pipeline"); }
    finally { setBusy(false); }
  }

  async function remove(projectId: string) {
    if (busy || !window.confirm("Delete this project and all of its pipeline history?")) return;
    setBusy(true); setError("");
    try {
      await api<void>(`/projects/${projectId}`, { method: "DELETE" });
      setProjects(previous => previous.filter(project => project.id !== projectId));
      setPipelines(previous => previous.filter(pipeline => pipeline.projectId !== projectId));
    } catch (e) { setError(e instanceof Error ? e.message : "Could not delete project"); }
    finally { setBusy(false); }
  }

  return <main>
    <nav><div className="brand">ForgeCloud <span>/ local</span></div><span className="milestone">MILESTONE 1</span></nav>
    <section className="hero">
      <p className="kicker">DEVELOPER CI/CD PLATFORM</p>
      <h1>Connect code.<br/>Queue the journey.</h1>
      <p>Register GitHub projects and trace pipeline triggers. Build execution arrives in the worker milestone.</p>
    </section>
    {error && <div className="error" role="alert">{error}</div>}
    <section className="grid">
      <article className="panel">
        <div className="heading"><h2>New project</h2><span>01</span></div>
        <form onSubmit={createProject}>
          <label>Project name<input required maxLength={100} value={name} onChange={e => setName(e.target.value)} placeholder="payment-service"/></label>
          <label>GitHub repository<input required type="url" value={repositoryUrl} onChange={e => setRepositoryUrl(e.target.value)} placeholder="https://github.com/user/repository"/></label>
          <label>Default branch<input required maxLength={200} value={branch} onChange={e => setBranch(e.target.value)}/></label>
          <button disabled={busy || loading}>{busy ? "Working…" : "Connect repository"}</button>
        </form>
      </article>
      <article className="panel wide">
        <div className="heading"><h2>Projects</h2><span>{String(projects.length).padStart(2, "0")}</span></div>
        {loading ? <p className="muted">Loading…</p> : projects.length === 0 ? <p className="empty">No projects connected yet.</p> :
          <div className="list">{projects.map(project => <div className="row" key={project.id}>
            <div><strong>{project.name}</strong><a href={project.repositoryUrl} target="_blank" rel="noreferrer">{project.repositoryFullName}</a><small>{project.branch}</small></div>
            <div className="actions"><button className="secondary" disabled={busy} onClick={() => trigger(project.id)}>Run pipeline</button><button className="danger" disabled={busy} onClick={() => remove(project.id)}>Delete</button></div>
          </div>)}</div>}
      </article>
    </section>
    <section className="panel pipelines">
      <div className="heading"><h2>Latest pipelines</h2><span>{String(pipelines.length).padStart(2, "0")}</span></div>
      {pipelines.length === 0 ? <p className="empty">A manual run or verified GitHub push will appear here.</p> :
        <div className="list">{pipelines.map(pipeline => <div className="pipeline" key={pipeline.id}>
          <span className={`status ${pipeline.status.toLowerCase()}`}>{pipeline.status}</span>
          <strong>{pipeline.projectName}</strong><span>{pipeline.branch}</span><span>{pipeline.triggerType.replace("_", " ")}</span>
          <code>{pipeline.commitSha?.slice(0, 8) ?? "manual"}</code>
        </div>)}</div>}
    </section>
    <footer>Pipeline records only. This milestone does not clone repositories or execute user code.</footer>
  </main>;
}

