import React, { useState } from "react";
import { request } from "../lib/api";

export default function AdminDashboardPage() {
  const [departments, setDepartments] = useState([]);
  const [users, setUsers] = useState([]);
  const [issues, setIssues] = useState([]);
  const [deptUser, setDeptUser] = useState({
    name: "",
    email: "",
    password: "",
    role: "OFFICER",
    departmentId: "",
  });
  const [assign, setAssign] = useState({ departmentId: "", userId: "" });
  const [issueAssign, setIssueAssign] = useState({
    issueId: "",
    departmentId: "",
  });
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const loadAll = async () => {
    setError("");
    setMessage("");
    const [dept, usr, iss] = await Promise.all([
      request("/api/v2/admin/departments", { auth: true }),
      request("/api/v2/admin/users", { auth: true }),
      request("/api/v2/admin/issues", { auth: true }),
    ]);
    setDepartments(dept || []);
    setUsers(usr || []);
    setIssues(iss || []);
  };

  const assignUser = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      await request(
        `/api/v2/admin/departments/${assign.departmentId}/users/${assign.userId}`,
        { method: "POST", auth: true }
      );
      setMessage("User assigned to department.");
    } catch (err) {
      setError(err.message);
    }
  };

  const assignIssue = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      await request("/api/v2/issues/assign-department", {
        method: "POST",
        auth: true,
        body: JSON.stringify(issueAssign),
      });
      setMessage("Issue assigned to department.");
    } catch (err) {
      setError(err.message);
    }
  };

  const createDeptUser = async (e) => {
    e.preventDefault();
    setError("");
    setMessage("");
    try {
      await request("/api/v2/admin/department-users", {
        method: "POST",
        auth: true,
        body: JSON.stringify(deptUser),
      });
      setMessage("Department user created.");
      setDeptUser({
        name: "",
        email: "",
        password: "",
        role: "OFFICER",
        departmentId: "",
      });
      loadAll();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <div className="stack">
      <section className="hero">
        <div>
          <h2>Admin Dashboard</h2>
          <p className="note">
            View departments, users, and issues. Assign responsibilities.
          </p>
        </div>
      </section>

      <div className="grid two-cols">
        <form className="form-card" onSubmit={createDeptUser}>
          <h3>Create Department Login</h3>
          <input
            placeholder="Name"
            value={deptUser.name}
            onChange={(e) =>
              setDeptUser((s) => ({ ...s, name: e.target.value }))
            }
            required
          />
          <input
            placeholder="Email"
            value={deptUser.email}
            onChange={(e) =>
              setDeptUser((s) => ({ ...s, email: e.target.value }))
            }
            required
          />
          <input
            placeholder="Password"
            type="password"
            value={deptUser.password}
            onChange={(e) =>
              setDeptUser((s) => ({ ...s, password: e.target.value }))
            }
            required
          />
          <select
            value={deptUser.role}
            onChange={(e) =>
              setDeptUser((s) => ({ ...s, role: e.target.value }))
            }
          >
            <option value="OFFICER">Officer</option>
            <option value="HEAD">Head</option>
          </select>
          <input
            placeholder="Department ID"
            value={deptUser.departmentId}
            onChange={(e) =>
              setDeptUser((s) => ({ ...s, departmentId: e.target.value }))
            }
          />
          <button type="submit">Create Login</button>
        </form>
        <form className="form-card" onSubmit={assignUser}>
          <h3>Assign User to Department</h3>
          <input
            placeholder="Department ID"
            value={assign.departmentId}
            onChange={(e) =>
              setAssign((s) => ({ ...s, departmentId: e.target.value }))
            }
          />
          <input
            placeholder="User ID"
            value={assign.userId}
            onChange={(e) =>
              setAssign((s) => ({ ...s, userId: e.target.value }))
            }
          />
          <button type="submit">Assign</button>
        </form>

        <form className="form-card" onSubmit={assignIssue}>
          <h3>Assign Issue to Department</h3>
          <input
            placeholder="Issue ID"
            value={issueAssign.issueId}
            onChange={(e) =>
              setIssueAssign((s) => ({ ...s, issueId: e.target.value }))
            }
          />
          <input
            placeholder="Department ID"
            value={issueAssign.departmentId}
            onChange={(e) =>
              setIssueAssign((s) => ({ ...s, departmentId: e.target.value }))
            }
          />
          <button type="submit">Assign</button>
        </form>
      </div>

      <button className="ghost" type="button" onClick={loadAll}>
        Refresh Data
      </button>
      {message ? <p className="success">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="grid two-cols">
        <div className="result-card">
          <h3>Departments</h3>
          <ul className="list">
            {departments.map((d) => (
              <li key={d.id}>
                <strong>{d.name}</strong>
                <span className="muted">ID {d.id}</span>
              </li>
            ))}
          </ul>
        </div>
        <div className="result-card">
          <h3>Users</h3>
          <ul className="list">
            {users.map((u) => (
              <li key={u.id}>
                <strong>{u.name}</strong>
                <span className="muted">
                  {u.email} • {u.role}
                </span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="result-card">
        <h3>Reported Issues</h3>
        <ul className="list">
          {issues.map((i) => (
            <li key={i.id}>
              <strong>{i.title}</strong>
              <span className="muted">
                ID {i.id} • {i.status}
              </span>
              <p>{i.description}</p>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
