import { useState } from "react";
import { request } from "../lib/api";
import { setToken } from "../lib/config";
import ResultPanel from "../components/ResultPanel";
import React from "react";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const token = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password }),
      });
      if (typeof token === "string") {
        setToken(token);
      }
      setResult({ token });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Login</h2>
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">Login</button>
      </form>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
