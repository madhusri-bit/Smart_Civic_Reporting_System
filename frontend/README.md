# Civic Reporting React Frontend

React (Vite) frontend to test backend functionalities end-to-end.

## Setup

1. Start backend (`http://localhost:8080`).
2. Open terminal in `frontend`:
   ```powershell
   npm install
   npm run dev
   ```
3. Open the URL shown by Vite (default `http://localhost:5173`).

## Pages Included

- Dashboard: recent API calls and success/failure summary.
- Login page: `POST /api/auth/login`.
- Register page: `POST /api/auth/register`.
- Image Upload page: `POST /api/issues/report-image` (JWT supported).
- Issue Report page:
  - `POST /api/issues/report` (JSON)
  - `POST /api/issues/report` (multipart)
- Location page: `POST /api/location` (JWT/userId).
- Community page: `POST /api/community`.
- Escalation page: `POST /api/escalations`.

## Config

Top panel in app lets you set:
- Backend Base URL
- JWT Token

Both are saved in browser localStorage.
