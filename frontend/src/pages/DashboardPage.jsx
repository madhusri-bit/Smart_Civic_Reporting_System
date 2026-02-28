import React, { useEffect, useState } from "react";
import { request } from "../lib/api";
import { getUser } from "../lib/config";

const emptyForm = {
  photo: null,
};

export default function DashboardPage() {
  const [user] = useState(getUser());
  const [form, setForm] = useState(emptyForm);
  const [issues, setIssues] = useState([]);
  const [selected, setSelected] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [result, setResult] = useState("");
  const [error, setError] = useState("");
  const [duplicate, setDuplicate] = useState(null);

  const loadIssues = async () => {
    if (!user) return;
    const data = await request("/api/v2/issues/my", { auth: true });
    setIssues(Array.isArray(data) ? data : []);
  };

  useEffect(() => {
    loadIssues();
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setResult("");
    setDuplicate(null);
    try {
      const fd = new FormData();
      fd.append("photo", form.photo);

      const data = await request("/api/v2/issues/report", {
        method: "POST",
        body: fd,
        isJson: false,
        auth: true,
      });

      if (data && data.status === "duplicate") {
        setDuplicate(data);
        return;
      }

      setResult("Issue reported successfully.");
      setForm(emptyForm);
      await loadIssues();
    } catch (err) {
      setError(err.message);
    }
  };

  const fetchTimeline = async (issueId) => {
    setSelected(issueId);
    const data = await request(`/api/v2/issues/${issueId}/timeline`, {
      auth: true,
    });
    setTimeline(Array.isArray(data) ? data : []);
  };

  const upvoteDuplicate = async () => {
    if (!duplicate?.existingIssueId) return;
    await request(`/api/v2/issues/${duplicate.existingIssueId}/upvote`, {
      method: "POST",
      auth: true,
    });
    setDuplicate(null);
    setResult("Upvoted the existing issue.");
  };

  return (
    <div className="stack">
      <section className="hero">
        <div>
          <h2>Citizen Dashboard</h2>
          <p className="note">
            Report an issue with a photo and track progress like order delivery.
          </p>
        </div>
        <div className="hero-card">
          <span>My Reports</span>
          <strong>{issues.length}</strong>
        </div>
      </section>

      <div className="grid two-cols">
        <form className="form-card" onSubmit={submit}>
          <h3>Report an Issue</h3>
          <input
            type="file"
            accept="image/*"
            onChange={(e) =>
              setForm((s) => ({ ...s, photo: e.target.files?.[0] || null }))
            }
            required
          />
          <button type="submit">Submit report</button>
          {result ? <p className="success">{result}</p> : null}
          {error ? <p className="error">{error}</p> : null}
          {duplicate ? (
            <div className="notice">
              <p>{duplicate.message}</p>
              <button type="button" onClick={upvoteDuplicate}>
                Upvote existing issue
              </button>
            </div>
          ) : null}
        </form>

        <div className="result-card">
          <h3>My Issues</h3>
          {issues.length === 0 ? (
            <p className="note">No issues yet.</p>
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
                    <button
                      className="ghost"
                      type="button"
                      onClick={() => fetchTimeline(issue.id)}
                    >
                      View progress
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {selected ? (
        <section className="result-card">
          <h3>Issue Progress</h3>
          {timeline.length === 0 ? (
            <p className="note">No status updates yet.</p>
          ) : (
            <div className="timeline">
              {timeline.map((step, idx) => (
                <div key={`${step.id}-${idx}`} className="timeline-step">
                  <span className="dot" />
                  <div>
                    <strong>{step.status}</strong>
                    <p className="note">{step.note || "Update posted"}</p>
                    <span className="muted">
                      {step.createdAt?.replace?.("T", " ")}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </section>
      ) : null}
    </div>
  );
}
