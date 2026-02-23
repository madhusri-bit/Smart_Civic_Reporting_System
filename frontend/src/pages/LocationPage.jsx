import { useState } from "react";
import { request } from "../lib/api";
import ResultPanel from "../components/ResultPanel";
import React from "react";

export default function LocationPage() {
  const [latitude, setLatitude] = useState("");
  const [longitude, setLongitude] = useState("");
  const [userId, setUserId] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const payload = {
        latitude: Number(latitude),
        longitude: Number(longitude),
      };
      if (userId) payload.userId = Number(userId);
      const data = await request("/api/location", {
        method: "POST",
        body: JSON.stringify(payload),
        auth: true,
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Save Location</h2>
        <input
          type="number"
          step="any"
          value={latitude}
          onChange={(e) => setLatitude(e.target.value)}
          placeholder="latitude"
          required
        />
        <input
          type="number"
          step="any"
          value={longitude}
          onChange={(e) => setLongitude(e.target.value)}
          placeholder="longitude"
          required
        />
        <input
          type="number"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          placeholder="userId (optional)"
        />
        <button type="submit">POST /api/location</button>
      </form>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
