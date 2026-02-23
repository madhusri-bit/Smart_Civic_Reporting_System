import { useState } from "react";
import { request } from "../lib/api";
import ResultPanel from "../components/ResultPanel";
import React from "react";

export default function CommunityPage() {
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const data = await request("/api/community", {
        method: "POST",
        body: JSON.stringify({ title, content }),
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Community Post</h2>
        <input
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          placeholder="title"
          required
        />
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="content"
          required
        />
        <button type="submit">POST /api/community</button>
      </form>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
