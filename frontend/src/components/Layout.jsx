import { Link, NavLink } from "react-router-dom";
import { getBaseUrl, getToken, setBaseUrl, setToken } from "../lib/config";
import { useState } from "react";
import React from "react";

const tabs = [
  ["/", "Dashboard"],
  ["/auth/login", "Login"],
  ["/auth/register", "Register"],
  ["/issues/image", "Image Upload"],
  ["/issues/report", "Issue Report"],
  ["/location", "Location"],
  ["/community", "Community"],
  ["/escalation", "Escalation"],
];

export default function Layout({ children }) {
  const [baseUrl, setBase] = useState(getBaseUrl());
  const [token, setTokenState] = useState(getToken());

  const save = () => {
    setBaseUrl(baseUrl);
    setToken(token);
    alert("Config saved");
  };

  return (
    <div className="shell">
      <header className="topbar">
        <div>
          <h1>Civic Reporting Console</h1>
          <p>React frontend to test all backend endpoints</p>
        </div>
        <Link to="/" className="brand-tag">
          API QA
        </Link>
      </header>

      <section className="config-card">
        <label>
          Backend URL
          <input value={baseUrl} onChange={(e) => setBase(e.target.value)} />
        </label>
        <label>
          JWT Token
          <input
            value={token}
            onChange={(e) => setTokenState(e.target.value)}
            placeholder="token only"
          />
        </label>
        <button onClick={save}>Save Config</button>
      </section>

      <nav className="nav">
        {tabs.map(([path, label]) => (
          <NavLink
            key={path}
            to={path}
            className={({ isActive }) => (isActive ? "active" : "")}
          >
            {label}
          </NavLink>
        ))}
      </nav>

      <section className="content">{children}</section>
    </div>
  );
}
