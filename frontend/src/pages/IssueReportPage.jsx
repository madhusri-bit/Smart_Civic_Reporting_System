import { useState } from "react";
import { request } from "../lib/api";
import ResultPanel from "../components/ResultPanel";
import React from "react";

export default function IssueReportPage() {
  const [jsonForm, setJsonForm] = useState({
    title: "",
    description: "",
    latitude: "",
    longitude: "",
    photoUrl: "",
  });
  const [mpForm, setMpForm] = useState({
    title: "",
    description: "",
    latitude: "",
    longitude: "",
    photo: null,
  });
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  async function submitJson(e) {
    e.preventDefault();
    setError("");
    try {
      const payload = {
        ...jsonForm,
        latitude: jsonForm.latitude ? Number(jsonForm.latitude) : null,
        longitude: jsonForm.longitude ? Number(jsonForm.longitude) : null,
      };
      const data = await request("/api/issues/report", {
        method: "POST",
        body: JSON.stringify(payload),
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  async function submitMultipart(e) {
    e.preventDefault();
    setError("");
    try {
      const fd = new FormData();
      fd.append("title", mpForm.title);
      if (mpForm.description) fd.append("description", mpForm.description);
      if (mpForm.latitude) fd.append("latitude", mpForm.latitude);
      if (mpForm.longitude) fd.append("longitude", mpForm.longitude);
      if (mpForm.photo) fd.append("photo", mpForm.photo);
      const data = await request("/api/issues/report", {
        method: "POST",
        body: fd,
        isJson: false,
      });
      setResult(data);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="stack">
      <div className="grid two-cols">
        <form className="form-card" onSubmit={submitJson}>
          <h2>Issue Report (JSON)</h2>
          <input
            placeholder="title"
            value={jsonForm.title}
            onChange={(e) =>
              setJsonForm((s) => ({ ...s, title: e.target.value }))
            }
            required
          />
          <textarea
            placeholder="description"
            value={jsonForm.description}
            onChange={(e) =>
              setJsonForm((s) => ({ ...s, description: e.target.value }))
            }
          />
          <input
            type="number"
            step="any"
            placeholder="latitude"
            value={jsonForm.latitude}
            onChange={(e) =>
              setJsonForm((s) => ({ ...s, latitude: e.target.value }))
            }
          />
          <input
            type="number"
            step="any"
            placeholder="longitude"
            value={jsonForm.longitude}
            onChange={(e) =>
              setJsonForm((s) => ({ ...s, longitude: e.target.value }))
            }
          />
          <input
            placeholder="photoUrl"
            value={jsonForm.photoUrl}
            onChange={(e) =>
              setJsonForm((s) => ({ ...s, photoUrl: e.target.value }))
            }
          />
          <button type="submit">Submit JSON Report</button>
        </form>

        <form className="form-card" onSubmit={submitMultipart}>
          <h2>Issue Report (Multipart)</h2>
          <input
            placeholder="title"
            value={mpForm.title}
            onChange={(e) =>
              setMpForm((s) => ({ ...s, title: e.target.value }))
            }
            required
          />
          <textarea
            placeholder="description"
            value={mpForm.description}
            onChange={(e) =>
              setMpForm((s) => ({ ...s, description: e.target.value }))
            }
          />
          <input
            type="number"
            step="any"
            placeholder="latitude"
            value={mpForm.latitude}
            onChange={(e) =>
              setMpForm((s) => ({ ...s, latitude: e.target.value }))
            }
          />
          <input
            type="number"
            step="any"
            placeholder="longitude"
            value={mpForm.longitude}
            onChange={(e) =>
              setMpForm((s) => ({ ...s, longitude: e.target.value }))
            }
          />
          <input
            type="file"
            accept="image/*"
            onChange={(e) =>
              setMpForm((s) => ({ ...s, photo: e.target.files?.[0] || null }))
            }
          />
          <button type="submit">Submit Multipart Report</button>
        </form>
      </div>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
