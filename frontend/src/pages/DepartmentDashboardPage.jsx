import React, { useState } from "react";
import { request } from "../lib/api";

export default function DepartmentDashboardPage() {
  const [departmentId, setDepartmentId] = useState("");
  const [issues, setIssues] = useState([]);
  const [resolveIssueId, setResolveIssueId] = useState("");
  const [resolvePhoto, setResolvePhoto] = useState(null);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadIssues = async () => {
    setError("");
    setMessage("");
    const data = await request(`/api/v2/issues/department/${departmentId}`, {
      auth: true,
    });
    setIssues(Array.isArray(data) ? data : []);
  };

  const resolve = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      const fd = new FormData();
      fd.append("photo", resolvePhoto);
      const data = await request(`/api/v2/issues/${resolveIssueId}/resolve`, {
        method: "POST",
        body: fd,
        auth: true,
        isJson: false,
      });
      setMessage(`Marked as waiting review. AI check: ${data.aiResolved}`);
      setResolveIssueId("");
      setResolvePhoto(null);
      loadIssues();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="stack">
      <section className="hero">
        <div>
          <h2>Department Dashboard</h2>
          <p className="note">View issues sorted by severity and resolve.</p>
        </div>
      </section>

      <div className="grid two-cols">
        <form className="form-card" onSubmit={(e) => e.preventDefault()}>
          <h3>Load Issues</h3>
          <input
            placeholder="Department ID"
            value={departmentId}
            onChange={(e) => setDepartmentId(e.target.value)}
          />
          <button type="button" onClick={loadIssues} disabled={!departmentId}>
            Fetch
          </button>
        </form>

        <form className="form-card" onSubmit={resolve}>
          <h3>Resolve Issue</h3>
          <input
            placeholder="Issue ID"
            value={resolveIssueId}
            onChange={(e) => setResolveIssueId(e.target.value)}
            required
          />
          <input
            type="file"
            accept="image/*"
            onChange={(e) => setResolvePhoto(e.target.files?.[0] || null)}
            required
          />
          <button type="submit">Upload resolved photo</button>
          {message ? <p className="success">{message}</p> : null}
          {error ? <p className="error">{error}</p> : null}
        </form>
      </div>

      <div className="result-card">
        <h3>Issues by Severity</h3>
        {issues.length === 0 ? (
          <p className="note">No issues loaded.</p>
        ) : (
          <ul className="list">
            {issues.map((issue) => (
              <li key={issue.id}>
                <div>
                  <strong>{issue.title}</strong>
                  <span className="muted">
                    Severity {issue.severity?.toFixed?.(1) || issue.severity}
                  </span>
                </div>
                <p>{issue.description || "No description provided."}</p>
                <div className="row">
                  <span className="chip">Upvotes {issue.upvotes}</span>
                  <span className="chip">Comments {issue.comments}</span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
