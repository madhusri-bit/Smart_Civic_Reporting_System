const BASE_URL_KEY = "http://localhost:8080";
const TOKEN_KEY = "civic_api_token";
const HISTORY_KEY = "civic_api_history";
const USER_KEY = "civic_user";

export function getBaseUrl() {
  return localStorage.getItem(BASE_URL_KEY) || "http://localhost:8080";
}

export function setBaseUrl(url) {
  localStorage.setItem(BASE_URL_KEY, (url || "").trim());
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || "";
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, (token || "").trim());
}

export function addHistory(entry) {
  const list = getHistory();
  list.unshift(entry);
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list.slice(0, 50)));
}

export function getHistory() {
  try {
    return JSON.parse(localStorage.getItem(HISTORY_KEY) || "[]");
  } catch {
    return [];
  }
}

export function setUser(user) {
  if (!user) {
    localStorage.removeItem(USER_KEY);
  } else {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY) || "null");
  } catch {
    return null;
  }
}
