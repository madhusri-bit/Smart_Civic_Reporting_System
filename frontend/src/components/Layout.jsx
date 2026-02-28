import { NavLink } from "react-router-dom";
import React from "react";
import { getUser, setToken, setUser } from "../lib/config";
import { useEffect, useState } from "react";

const baseTabs = [
  ["/", "Dashboard"],
  ["/community", "Community"],
];

export default function Layout({ children }) {
  const [user, setUserState] = useState(getUser());

  const role = user?.role || "GUEST";

  const tabs = [...baseTabs];
  if (role === "ADMIN") {
    tabs.push(["/admin", "Admin"]);
  }
  if (role === "OFFICER" || role === "HEAD") {
    tabs.push(["/department", "Department"]);
  }
  if (!user) {
    tabs.push(["/auth/login", "Login"]);
    tabs.push(["/auth/register", "Register"]);
  }

  const logout = () => {
    setToken("");
    setUser(null);
    setUserState(null);
  };

  useEffect(() => {
    const onStorage = () => setUserState(getUser());
    window.addEventListener("storage", onStorage);
    return () => window.removeEventListener("storage", onStorage);
  }, []);

  return (
    <div className="shell">
      <header className="topbar">
        <div>
          <h1>Civic Reporting System</h1>
          <p>Simple portal for citizens to report issues</p>
        </div>
        <div className="header-actions">
          <span className="pill-role">{role}</span>
          {user ? (
            <button className="ghost" type="button" onClick={logout}>
              Logout
            </button>
          ) : null}
        </div>
      </header>

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
