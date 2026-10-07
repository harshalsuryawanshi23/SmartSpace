# SmartSpace — Smart Space & Resource Availability

**A community-hall discovery and verified-entry booking platform.**
Find the nearest small hall, book a 2–4 hour slot, and enter on event day with a signed QR code plus an identity check run by the hall's watchman.

> *Category:* Facilities Management · *Stack:* React · Spring Boot · MySQL

---

## 1. What it does

Small society halls sit idle most of the week while residents struggle to find affordable, nearby venues for birthdays, kitty parties, tuition batches and meetings. SmartSpace gives them:

- **Discovery** — location-based search with filters and a live availability calendar.
- **Micro-booking** — 30-minute cell engine, temporary slot locking, zero double-booking (enforced by the database).
- **Dual-layer entry** — time-bound signed QR **+** OTP to the KYC-verified phone, verified by a watchman on a phone-first PWA (works offline).
- **Decorator matching** — suggestions based on the hall's real capacity/layout and the event type, with a "why matched" explanation.
- **Trust** — reputation built only from check-in-confirmed stays, for renters, halls and decorators.
- **Accountability** — check-in/out timestamps, checklist, photos and an evidence PDF instead of a formal deposit.
- **Owner insights** — occupancy, revenue, heatmap and a Smart Pricing Advisor.
- **Admin** — approvals, disputes, platform analytics.

### Added novelty features (beyond the project document)
Offline-resilient watchman PWA · Live Capacity Guard · Quiet-Hours & Overstay Guard · Decorator Access Pass with setup/teardown buffers · Resident-Priority Windows & Community Pricing · Waitlist & Smart Alternatives · Handover Evidence Pack · Multilingual (EN/हिन्दी/मराठी) low-literacy UI · Privacy Dashboard. Details in `PRD.md` §5 and `IMPLEMENTATION.md` §19.

## 2. Documentation map (read in this order)

| File | Purpose |
|------|---------|
| `README.md` | This file — overview and how to run |
| `PRD.md` | **What & why** — problem, users, scope, requirements, acceptance criteria |
| `ARCHITECTURE.md` | **How** — fixed stack, layering, data model, flows, security, deployment |
| `DESIGN.md` | **Look & feel** — tokens, components, screens, watchman-first design, a11y |
| `RULES.md` | **How the AI must code** — rules, definition of done, reporting format |
| `TASKS.md` | **What to build next** — phased, numbered tasks with acceptance criteria |
| `IMPLEMENTATION.md` | **Build detail** — DDL, algorithms, API contract, frontend, tests, DevOps, demo |
| `.env.example` | Required configuration (no secrets) |
| `docs/DECISIONS.md`, `docs/MEMORY.md` | Created during the build: decisions and current project state |

## 3. Tech stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18, TypeScript, Vite, Tailwind CSS 3.4, TanStack Query, React Router, i18next, Leaflet/OpenStreetMap, PWA |
| Backend | Spring Boot 3 (Java 21), Spring Security (JWT), Spring Data JPA, Flyway |
| Database | MySQL 8.4 |
| Integrations | Payment gateway (Razorpay test mode / mock), identity provider (mock), SMTP + SMS provider abstraction |
| Testing | JUnit 5, Testcontainers, Vitest, Playwright, axe |
| Ops | Docker Compose, Caddy/Nginx (TLS), GitHub Actions |

## 4. Quick start

### Prerequisites
Docker + Docker Compose, Git. (For local dev without Docker: JDK 21, Node 22 LTS, MySQL 8.4.)

### Run everything (dev profile: mock KYC/payment/SMS, seed data, MailHog)
```bash
git clone <your-repo-url> smart-space && cd smart-space
cp .env.example .env            # fill the empty values (see comments in the file)
bash infra/scripts/gen-qr-keys.sh   # creates Ed25519 QR signing keys in infra/keys/
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

| Service | URL |
|---------|-----|
| Web app | https://smartspace.local (or http://localhost:5173 in Vite dev mode) |
| API docs (Swagger UI) | https://smartspace.local/api/swagger-ui.html |
| MailHog (emails/OTP) | http://localhost:8025 |
| Adminer (DB UI, dev) | http://localhost:8081 |

> **HTTPS matters:** phone browsers only allow camera scanning on secure origins. `localhost` works for development; for LAN demos use Caddy's internal CA or `mkcert` and install the root certificate on the watchman phone (`ARCHITECTURE.md` §10).

### Local development (no Docker for the apps)
```bash
# backend
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# frontend
cd frontend && npm ci && npm run dev
```

### Tests & quality
```bash
cd backend  && ./mvnw -q verify                                   # unit + integration (Testcontainers)
cd frontend && npm run lint && npm run typecheck && npm test && npm run build
cd e2e      && npx playwright test                                # needs the compose stack running
```

## 5. Demo accounts (dev seed only)

Password for all: value of `SEED_PASSWORD` (default `Demo@12345`).

| Role | Email |
|------|-------|
| Admin | `admin@smartspace.test` |
| Hall owner | `owner.meadows@smartspace.test` |
| Watchman | `watch.meadows@smartspace.test` |
| Resident (KYC verified) | `priya@smartspace.test` |
| Resident (not verified) | `rahul@smartspace.test` |
| Decorator | `rang.decor@smartspace.test` |

A scripted 10-minute walkthrough is in `IMPLEMENTATION.md` §24.2. In dev, `POST /dev/reset-demo` reseeds data and can fast-forward the server clock.

## 6. Project structure

```
smart-space/
├── backend/    Spring Boot API (package-by-feature: web → service → repo)
├── frontend/   React app + watchman PWA (feature-first folders)
├── e2e/        Playwright journeys
├── infra/      proxy config, scripts (keys, backup/restore), MySQL init
├── docker-compose.prod.yml  Production environment deployment configuration
├── docs/       DECISIONS.md, MEMORY.md, exported OpenAPI
└── *.md        the specification set listed above
```

## 7. Security & privacy notes

- **No raw Aadhaar number, biometric or Aadhaar photo is ever stored or logged.** Only a provider reference, verified name, masked suffix, status and timestamps.
- The build ships a **mock identity provider**. Real Aadhaar authentication/e-KYC is a regulated activity; obtain legal review and use a lawful provider before any real deployment (`PRD.md` R1).
- QR tokens carry no personal data, are signed (Ed25519), time-windowed and revocable.
- The QR+OTP check gives **strong evidence** that the entrant is the booker; it cannot give absolute proof, and the UI says so.
- Secrets live only in `.env` (git-ignored). Never commit keys, dumps or uploads.

## 8. Deployment (in-house)

Docker Compose on a Linux host behind Caddy/Nginx with TLS using `docker-compose.prod.yml`. Nightly `mysqldump` backups are enabled via `infra/scripts/backup.sh`, with restoration capabilities in `infra/scripts/restore.sh`. Flyway runs migrations on start. See `ARCHITECTURE.md` §10 and `IMPLEMENTATION.md` §23.

## 9. Working with an AI coding agent

1. Put all spec files in the repository root.
2. Paste the kickoff prompt from `IMPLEMENTATION.md` §0.1 and approve the Phase 0 plan.
3. Run one phase at a time using the prompt template in `RULES.md` §11; merge only when the phase exit criteria in `TASKS.md` pass.
4. Keep `TASKS.md` and `docs/MEMORY.md` current.

## 10. Roadmap

P0 MVP → P1 hardening of document novelties → P2 added novelties → P3 stretch (Split-the-Cost, natural-language brief, Web Push). Full plan: `TASKS.md`.

## 11. Team, licence

_Team members, guide and institution: add here._ · _Licence: add here (e.g., MIT for code; academic-use note if required)._
