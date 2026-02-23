import { useState } from "react";
import { request } from "../lib/api";
import { setToken } from "../lib/config";
import ResultPanel from "../components/ResultPanel";
import React from "react";

const initial = {
  name: "",
  email: "",
  password: "",
  role: "CITIZEN",
  preferredLanguage: "",
  state: "",
  city: "",
  pincode: "",
  address: "",
  profile: "",
};

export default function RegisterPage() {
  const [form, setForm] = useState(initial);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  const onChange = (e) =>
    setForm((s) => ({ ...s, [e.target.name]: e.target.value }));

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const token = await request("/api/auth/register", {
        method: "POST",
        body: JSON.stringify(form),
      });
      if (typeof token === "string") setToken(token);
      setResult({ token });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid two-cols">
      <form className="form-card" onSubmit={submit}>
        <h2>Register</h2>
        {Object.keys(initial).map((key) => (
          <input
            key={key}
            name={key}
            type={key === "password" ? "password" : "text"}
            placeholder={key}
            value={form[key]}
            onChange={onChange}
            required={["name", "email", "password"].includes(key)}
          />
        ))}
        <button type="submit">Register</button>
      </form>
      <ResultPanel data={result} error={error} />
    </div>
  );
}
