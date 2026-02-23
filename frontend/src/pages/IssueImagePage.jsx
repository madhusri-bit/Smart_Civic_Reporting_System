import { useState } from "react";
import { request } from "../lib/api";
import ResultPanel from "../components/ResultPanel";
import React from "react";

export default function IssueImagePage() {
  const [photo, setPhoto] = useState(null);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    if (!photo) return;
    setError("");
    const fd = new FormData();
    fd.append("photo", photo);
    try {
      const data = await request("/api/issues/report-image", {
        method: "POST",
        body: fd,
        auth: true,
        isJson: false,
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Upload Image Issue</h2>
        <p className="note">
          Uses `/api/issues/report-image` with JWT + EXIF + Gemini flow.
        </p>
        <input
          type="file"
          accept="image/*"
          required
          onChange={(e) => setPhoto(e.target.files?.[0] || null)}
        />
        <button type="submit">Upload Image</button>
      </form>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
