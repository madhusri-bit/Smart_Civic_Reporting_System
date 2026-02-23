import { useState } from "react";
import { request } from "../lib/api";
import ResultPanel from "../components/ResultPanel";
import React from "react";

export default function EscalationPage() {
  const [level, setLevel] = useState("HIGH");
  const [reason, setReason] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const data = await request("/api/escalations", {
        method: "POST",
        body: JSON.stringify({ level, reason }),
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Escalation</h2>
        <input
          value={level}
          onChange={(e) => setLevel(e.target.value)}
          placeholder="level"
          required
        />
        <textarea
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          placeholder="reason"
          required
        />
        <button type="submit">POST /api/escalations</button>
      </form>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
