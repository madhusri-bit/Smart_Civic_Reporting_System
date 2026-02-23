import React from "react";
export default function ResultPanel({ title = "Response", data, error }) {
  return (
    <div className="result-card">
      <h3>{title}</h3>
      {error ? <p className="error">{error}</p> : null}
      <pre>{data ? JSON.stringify(data, null, 2) : "No response yet."}</pre>
    </div>
  );
}
