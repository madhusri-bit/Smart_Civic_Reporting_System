import { addHistory, getBaseUrl, getToken } from "./config";

function parseResponse(text) {
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export async function request(path, { method = "GET", body, auth = false, isJson = true } = {}) {
  const headers = {};
  if (isJson) headers["Content-Type"] = "application/json";
  if (auth && getToken()) headers.Authorization = `Bearer ${getToken()}`;

  const response = await fetch(`${getBaseUrl().replace(/\/+$/, "")}${path}`, {
    method,
    headers,
    body,
  });

  const raw = await response.text();
  const data = parseResponse(raw);

  addHistory({
    at: new Date().toISOString(),
    method,
    path,
    status: response.status,
    ok: response.ok,
  });

  if (!response.ok) {
    throw new Error(typeof data === "string" ? data : JSON.stringify(data));
  }

  return data;
}

