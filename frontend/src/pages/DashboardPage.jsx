import { getHistory } from "../lib/config";
import React from "react";

export default function DashboardPage() {
  const history = getHistory();
  const total = history.length;
  const success = history.filter((h) => h.ok).length;
  const failed = total - success;

  return (
    <div className="stack">
      <h2>Dashboard</h2>
      <div className="stats">
        <article>
          <span>Total Calls</span>
          <strong>{total}</strong>
        </article>
        <article>
          <span>Success</span>
          <strong>{success}</strong>
        </article>
        <article>
          <span>Failed</span>
          <strong>{failed}</strong>
        </article>
      </div>

      <div className="result-card">
        <h3>Recent API Activity</h3>
        <table>
          <thead>
            <tr>
              <th>Time</th>
              <th>Method</th>
              <th>Path</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {history.slice(0, 10).map((item, i) => (
              <tr key={`${item.at}-${i}`}>
                <td>{new Date(item.at).toLocaleTimeString()}</td>
                <td>{item.method}</td>
                <td>{item.path}</td>
                <td>{item.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
