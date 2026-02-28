import { useState } from "react";
import React from "react";
import { request } from "../lib/api";
import { setToken, setUser } from "../lib/config";

const initial = {
  name: "",
  email: "",
  password: "",
  phone: "",
  address: "",
};

export default function RegisterPage() {
  const [form, setForm] = useState(initial);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const onChange = (e) =>
    setForm((s) => ({ ...s, [e.target.name]: e.target.value }));

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      const token = await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          name: form.name,
          email: form.email,
          password: form.password,
          address: form.address,
          phone: form.phone,
          role: "CITIZEN",
        }),
      });
      if (typeof token === "string") {
        setToken(token);
      }
      const me = await request("/api/v2/users/me", { auth: true });
      setUser(me);
      window.dispatchEvent(new Event("storage"));
      setMessage("Account created.");
      setForm(initial);
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Create account</h2>
        <input
          name="name"
          placeholder="Full name"
          value={form.name}
          onChange={onChange}
          required
        />
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
          placeholder="Create password"
          value={form.password}
          onChange={onChange}
          required
        />
        <input
          name="phone"
          placeholder="Phone number"
          value={form.phone}
          onChange={onChange}
        />
        <input
          name="address"
          placeholder="Address"
          value={form.address}
          onChange={onChange}
        />
        <button type="submit">Register</button>
      </form>
      <div className="result-card">
        <h3>Status</h3>
        <p className="note">
          This is a simple registration form. Backend integration can be added
          later.
        </p>
        {message ? <p className="success">{message}</p> : null}
        {error ? <p className="error">{error}</p> : null}
      </div>
    </div>
  );
}
