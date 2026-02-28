import { useState } from "react";
import React from "react";
import { request } from "../lib/api";
import { setToken, setUser } from "../lib/config";

export default function LoginPage() {
  const [form, setForm] = useState({ email: "", password: "" });
  const [adminForm, setAdminForm] = useState({
    name: "",
    email: "",
    password: "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [adminMessage, setAdminMessage] = useState("");
  const [adminError, setAdminError] = useState("");

  const onChange = (e) =>
    setForm((s) => ({ ...s, [e.target.name]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      const token = await request("/api/auth/login", {
        method: "POST",
        body: JSON.stringify(form),
      });
      if (typeof token === "string") {
        setToken(token);
      }
      const me = await request("/api/v2/users/me", { auth: true });
      setUser(me);
      window.dispatchEvent(new Event("storage"));
      setMessage("Login successful.");
    } catch (err) {
      setError(err.message);
    }
  };

  const bootstrapAdmin = async (e) => {
    e.preventDefault();
    setAdminError("");
    setAdminMessage("");
    try {
      await request("/api/v2/admin/bootstrap", {
        method: "POST",
        body: JSON.stringify(adminForm),
      });
      setAdminMessage("Admin account created. You can log in now.");
      setAdminForm({ name: "", email: "", password: "" });
    } catch (err) {
      setAdminError(err.message);
    }
  };

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Login</h2>
        <input
          name="email"
          type="email"
          placeholder="Email address"
          value={form.email}
          onChange={onChange}
          required
        />
        <input
          name="password"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={onChange}
          required
        />
        <button type="submit">Sign in</button>
      </form>
      <div className="stack">
        <div className="result-card">
          <h3>Status</h3>
          <p className="note">Use your registered email and password.</p>
          {message ? <p className="success">{message}</p> : null}
          {error ? <p className="error">{error}</p> : null}
        </div>
        <form className="form-card" onSubmit={bootstrapAdmin}>
          <h3>Create First Admin</h3>
          <p className="note">
            Use this only once. After an admin exists, this endpoint is blocked.
          </p>
          <input
            placeholder="Name"
            value={adminForm.name}
            onChange={(e) =>
              setAdminForm((s) => ({ ...s, name: e.target.value }))
            }
          />
          <input
            placeholder="Admin email"
            type="email"
            value={adminForm.email}
            onChange={(e) =>
              setAdminForm((s) => ({ ...s, email: e.target.value }))
            }
            required
          />
          <input
            placeholder="Admin password"
            type="password"
            value={adminForm.password}
            onChange={(e) =>
              setAdminForm((s) => ({ ...s, password: e.target.value }))
            }
            required
          />
          <button type="submit">Create Admin</button>
          {adminMessage ? <p className="success">{adminMessage}</p> : null}
          {adminError ? <p className="error">{adminError}</p> : null}
        </form>
      </div>
    </div>
  );
}
