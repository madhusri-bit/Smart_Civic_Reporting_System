import React from "react";
const baseUrlInput = document.getElementById("baseUrl");
const tokenInput = document.getElementById("token");
const output = document.getElementById("output");

function loadConfig() {
  const savedBase = localStorage.getItem("apiBaseUrl");
  const savedToken = localStorage.getItem("apiToken");
  if (savedBase) baseUrlInput.value = savedBase;
  if (savedToken) tokenInput.value = savedToken;
}

function saveConfig() {
  localStorage.setItem("apiBaseUrl", baseUrlInput.value.trim());
  localStorage.setItem("apiToken", tokenInput.value.trim());
  printResult("Config saved.", { baseUrl: baseUrlInput.value, hasToken: !!tokenInput.value });
}

function getBaseUrl() {
  return baseUrlInput.value.trim().replace(/\/+$/, "");
}

function getHeaders(withAuth = false, isJson = true) {
  const headers = {};
  if (isJson) headers["Content-Type"] = "application/json";
  if (withAuth && tokenInput.value.trim()) {
    headers["Authorization"] = `Bearer ${tokenInput.value.trim()}`;
  }
  return headers;
}

async function callApi({ method, path, body, withAuth = false, isJson = true }) {
  const response = await fetch(`${getBaseUrl()}${path}`, {
    method,
    headers: getHeaders(withAuth, isJson),
    body,
  });

  const text = await response.text();
  let parsed = text;
  try {
    parsed = JSON.parse(text);
  } catch (_) {}

  printResult(`${method} ${path}`, {
    status: response.status,
    ok: response.ok,
    data: parsed,
  });

  if (!response.ok) throw new Error(`Request failed (${response.status})`);
  return parsed;
}

function printResult(label, data) {
  output.textContent = `${label}\n\n${JSON.stringify(data, null, 2)}`;
}

function formToObject(form) {
  const fd = new FormData(form);
  const obj = {};
  for (const [k, v] of fd.entries()) {
    if (v === "") continue;
    obj[k] = v;
  }
  return obj;
}

document.getElementById("saveConfig").addEventListener("click", saveConfig);

document.getElementById("registerForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = JSON.stringify(formToObject(e.target));
  const token = await callApi({ method: "POST", path: "/api/auth/register", body });
  if (typeof token === "string") {
    tokenInput.value = token;
    saveConfig();
  }
});

document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = JSON.stringify(formToObject(e.target));
  const token = await callApi({ method: "POST", path: "/api/auth/login", body });
  if (typeof token === "string") {
    tokenInput.value = token;
    saveConfig();
  }
});

document.getElementById("locationForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = formToObject(e.target);
  if (payload.latitude) payload.latitude = Number(payload.latitude);
  if (payload.longitude) payload.longitude = Number(payload.longitude);
  if (payload.userId) payload.userId = Number(payload.userId);
  await callApi({
    method: "POST",
    path: "/api/location",
    body: JSON.stringify(payload),
    withAuth: true,
  });
});

document.getElementById("issueJsonForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const payload = formToObject(e.target);
  if (payload.latitude) payload.latitude = Number(payload.latitude);
  if (payload.longitude) payload.longitude = Number(payload.longitude);
  await callApi({
    method: "POST",
    path: "/api/issues/report",
    body: JSON.stringify(payload),
  });
});

document.getElementById("issueMultipartForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  if (!fd.get("photo") || fd.get("photo").size === 0) fd.delete("photo");
  await callApi({
    method: "POST",
    path: "/api/issues/report",
    body: fd,
    isJson: false,
  });
});

document.getElementById("reportImageForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  await callApi({
    method: "POST",
    path: "/api/issues/report-image",
    body: fd,
    isJson: false,
    withAuth: true,
  });
});

document.getElementById("communityForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = JSON.stringify(formToObject(e.target));
  await callApi({ method: "POST", path: "/api/community", body });
});

document.getElementById("escalationForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  const body = JSON.stringify(formToObject(e.target));
  await callApi({ method: "POST", path: "/api/escalations", body });
});

loadConfig();
