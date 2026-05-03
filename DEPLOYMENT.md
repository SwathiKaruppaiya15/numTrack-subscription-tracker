# SubTrack — Deployment Guide

## Local Development

```bash
# 1. Start PostgreSQL
docker run -d --name subtrack-pg \
  -e POSTGRES_DB=subtrack \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:16-alpine

# 2. Start backend
cd subtrack-backend
cp .env.example .env   # fill in values
mvn spring-boot:run

# 3. Start frontend
cd subtrack-frontend
cp .env.example .env
npm run dev
```

## Docker Compose (Full Stack)

```bash
cp .env.example .env   # fill in all values
docker-compose up --build
# Backend:  http://localhost:8080
# Frontend: http://localhost:5173
```

---

## Prompt 44 — Deploy Backend on Railway

1. Push code to GitHub
2. Go to https://railway.app → New Project → Deploy from GitHub
3. Select `subtrack-backend` folder
4. Add environment variables:
   - `DB_URL` → Railway PostgreSQL connection string
   - `DB_USERNAME`, `DB_PASSWORD`
   - `JWT_SECRET` → `openssl rand -hex 32`
   - `MAIL_USERNAME`, `MAIL_PASSWORD`
   - `FRONTEND_URL` → your Vercel URL
5. Railway auto-detects Dockerfile and builds
6. Note the generated URL (e.g. `https://subtrack-backend.up.railway.app`)

---

## Prompt 45 — Deploy Frontend on Vercel

1. Push code to GitHub
2. Go to https://vercel.com → New Project → Import from GitHub
3. Select `subtrack-frontend` folder
4. Set environment variable:
   - `VITE_API_BASE_URL` → your Railway backend URL
5. Vercel auto-detects Vite and deploys
6. Done — live at `https://subtrack.vercel.app`

---

## Prompt 46 — Production PostgreSQL Setup

### Option A: Railway PostgreSQL (recommended)
1. In Railway project → Add Service → Database → PostgreSQL
2. Copy `DATABASE_URL` from Railway dashboard
3. Set as `DB_URL` in backend environment

### Option B: Supabase
1. Create project at https://supabase.com
2. Go to Settings → Database → Connection string
3. Use the connection pooler URL as `DB_URL`

### Production checklist
- [ ] `ddl-auto: validate` (not `update`) in production
- [ ] Enable SSL: `?sslmode=require` in DB_URL
- [ ] Set strong `JWT_SECRET` (64+ hex chars)
- [ ] Use Gmail App Password (not account password) for SMTP
- [ ] Set `SCHEDULER_ENABLED=true`
