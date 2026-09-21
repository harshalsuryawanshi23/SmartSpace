# Development Rules (AI Rulebook)

> These rules apply to **every** task performed by an AI coding agent (Antigravity or otherwise) on this repository. If a rule here conflicts with a chat instruction, **stop and ask** — do not silently choose.

## 0. Read Order (mandatory before any change)

1. `README.md` — what this repo is
2. `PRD.md` — what and why
3. `ARCHITECTURE.md` — how (fixed decisions)
4. `DESIGN.md` — visual system
5. `RULES.md` — this file
6. `TASKS.md` — what to do next
7. `IMPLEMENTATION.md` — detailed specs (read the sections referenced by the task)

If `docs/DECISIONS.md` or `docs/MEMORY.md` exist, read them too.

## 1. General

- Use **Java 21 + Spring Boot 3.x** for the backend and **TypeScript (strict)** for the frontend. No JavaScript source files in `frontend/src`.
- **Do not change the tech stack.** No new framework, ORM, state library, UI kit or database. Need a new dependency? Add it only if it is necessary, list it (name, version, reason) in `docs/DECISIONS.md`, and mention it in your report.
- **One task at a time.** Work only on the task(s) named in the prompt. Do not "helpfully" implement upcoming tasks.
- **Do not modify unrelated files.** Keep diffs small and focused.
- **Reuse before creating.** Search the repo for an existing component/service/util first.
- Keep functions small (≈ 40 lines) and classes cohesive. No duplicated logic.
- **Never fabricate.** If a requirement is missing or ambiguous, list the question, propose a default, and continue only if the default is low-risk; otherwise stop and ask.
- No dead code, no commented-out blocks, no `TODO` without a matching entry in `TASKS.md`.
- Prefer clarity over cleverness. Names are full words (`bookingStartAt`, not `bsa`).

## 2. Before Coding

1. Read the docs in §0 and the code you will touch.
2. State the plan: files to create/change, data changes (migrations), API changes, tests to add.
3. For changes touching more than ~5 files or any DB schema: **write the plan first and wait for approval** (or record it as a plan artifact) before editing.
4. Confirm the task's **acceptance criteria** in `TASKS.md`.

## 3. Backend Rules

### Structure
- Follow the package layout in `ARCHITECTURE.md` §3.1: `web → service → repo`, with `domain` and `mapper` per feature.
- Controllers: HTTP concerns only (validation, auth annotations, DTO mapping). **No business logic, no repository calls.**
- Services: `@Transactional` boundaries and all business rules. Read-only queries use `@Transactional(readOnly = true)`.
- **Never return JPA entities from controllers.** Use records for DTOs (`record CreateBookingRequest(...)`).
- Cross-feature calls go through the other feature's **service interface**.
- Use constructor injection; no field injection.
- Use the injected `Clock` for time. **Never call `Instant.now()` / `LocalDateTime.now()` directly** outside the `Clock` provider.
- Money is `BigDecimal` scale 2, `RoundingMode.HALF_UP`. Never `double`/`float` for money.
- IDs exposed to clients are **UUID public ids**, not auto-increment PKs.

### API
- Base path `/api/v1`. Nouns, plural, kebab-case. Standard HTTP verbs/status codes.
- Errors use one shape: `{ "timestamp", "status", "code", "message", "details": [...], "traceId" }`. Codes are stable strings (`SLOT_UNAVAILABLE`, `KYC_REQUIRED`, `QR_EXPIRED`…) listed in `IMPLEMENTATION.md` §20.
- Validate every request with Bean Validation; validate business invariants in services.
- Paginate lists (`page`, `size` ≤ 100, `sort`). Never return unbounded lists.
- Document endpoints with springdoc annotations; the OpenAPI spec must build without warnings.

### Database
- **All schema changes via Flyway migrations** (`V{n}__{description}.sql`). Never edit an applied migration; add a new one. Never rely on `ddl-auto` other than `validate`.
- Every foreign key and every column used in `WHERE`/`ORDER BY` of a hot query has an index.
- Use `utf8mb4`, InnoDB, `DATETIME(3)` in **UTC**.
- Enforce integrity in the DB (unique, FK, check constraints) — not only in code. Slot uniqueness is `UNIQUE (hall_id, cell_start)`; do not replace it with app-level checks.
- No `SELECT *` in native queries. No N+1: use fetch joins/projections and verify with a test or SQL log.

### Booking & Payment invariants (never violate)
- Booking status changes **only** through `BookingStateMachine`; each change writes a `booking_events` row in the same transaction.
- Confirm a booking and issue its QR **atomically** after payment verification.
- Payment verify and webhook handlers are **idempotent**; verify signatures with constant-time comparison.
- Never trust client-supplied prices, totals, or status. Recompute server-side.

## 4. Frontend Rules

- Follow `DESIGN.md` tokens and components. **No hard-coded hex colours or one-off styles**; use Tailwind tokens.
- Feature-first folders (`ARCHITECTURE.md` §4). Data fetching only via feature hooks using TanStack Query; **no `fetch` inside components.**
- Forms: React Hook Form + Zod; show inline errors; disable submit while loading.
- **Every screen implements loading, empty, and error states** and is responsive at 375 / 768 / 1440.
- All user-visible strings go through `react-i18next` (`en`, `hi`, `mr`). No literal UI strings in JSX except in tests.
- Access token lives **in memory only**. Never put tokens, PII, or secrets in `localStorage`/`sessionStorage`. IndexedDB is allowed **only** for the watchman offline manifest/queue (N-01), which contains no raw ID data. The service worker (Cache Storage) may cache the renter's PII-free QR token response so it displays offline.
- Accessibility: semantic HTML, labelled inputs, keyboard navigable, focus visible, `aria-live` for async errors.
- Do not render server HTML with `dangerouslySetInnerHTML`.
- Watchman screens: minimal UI, ≥ 64 px primary controls, verdict screens per `DESIGN.md` §9. Do not add tables/filters there.

## 5. Security Rules (non-negotiable)

- **Never store, log, or transmit a raw Aadhaar number, biometrics, or an Aadhaar photo.** The KYC module keeps only provider reference, verified name, masked suffix, status, timestamps, consent id.
- **Never commit secrets.** Only `.env.example` (empty values) is committed. `.env`, `*.pem`, `uploads/`, and dumps are git-ignored.
- **Never expose API keys/secrets to client code.** Only `VITE_`-prefixed public values reach the browser.
- Verify authorisation **server-side** on every endpoint: role **and** ownership (e.g., owner of that hall, staff of that hall, holder of that booking).
- Validate and sanitise all input; use parameterised queries only.
- Log with correlation id; **mask** phone/email in logs; never log tokens, OTPs, QR tokens or passwords.
- QR tokens contain **no PII**. OTPs are hashed at rest, attempt-limited, and short-lived.
- Rate limit auth, OTP, scan, and enquiry endpoints.
- Uploaded files: validate type (magic bytes) and size, randomise names, store outside static roots.
- Treat data from webhooks, offline sync and clients as **untrusted** — re-validate on the server.
- Any suspected security issue found while working: stop and report.

## 6. Testing Rules

- Add tests for every important behaviour **in the same task**. A task is not done without them.
- Backend: unit tests for services/domain (JUnit 5 + Mockito); integration tests with **Testcontainers MySQL** for repositories, slot engine, booking flow, and security (RBAC/ownership).
- **Concurrency test** for the slot engine (≥ 50 threads, exactly one winner) must exist and pass.
- Frontend: Vitest + React Testing Library for components/hooks; Playwright for E2E journeys listed in `IMPLEMENTATION.md` §22.
- Tests must be deterministic (fixed `Clock`, seeded data), independent, and fast.
- After implementing, run and pass, in order:
  ```
  # backend
  ./mvnw -q verify
  # frontend
  npm run lint && npm run typecheck && npm test && npm run build
  ```
- Fix failing tests before starting the next task. Never disable or delete a test to make a build pass.

## 7. Git Rules

- Work on a branch per phase/feature: `feature/<phase>-<slug>`, `fix/<slug>`.
- **Small commits**, one logical change each. Conventional Commits:
  `feat: add slot lock expiry job`, `fix: correct cancellation refund window`, `test: add concurrent booking test`, `docs: update TASKS`, `chore: bump deps`, `refactor: extract PriceCalculator`.
- Never commit generated build output, secrets, or IDE files.
- Never force-push shared branches. Never rewrite history unless asked.

## 8. Definition of Done (every task)

- [ ] Acceptance criteria in `TASKS.md` met
- [ ] Code follows `ARCHITECTURE.md` layering and these rules
- [ ] UI follows `DESIGN.md` (with loading/empty/error states, responsive, i18n keys added)
- [ ] Tests added and **all** tests, lint, typecheck, and build pass
- [ ] Flyway migration added if schema changed; no manual DB steps
- [ ] No secrets, no console logs left, no dead code
- [ ] `TASKS.md` updated (checkbox ticked, notes if scope changed)
- [ ] `docs/MEMORY.md` updated (create it if missing): current status, decisions, known issues, next step
- [ ] Committed with a Conventional Commit message

## 9. Reporting Format (end of every task)

Report exactly:
1. **Files changed** (created / modified / deleted)
2. **What was implemented** (short)
3. **Tests executed** (commands + results)
4. **Deviations from spec** (and why) and **assumptions made**
5. **Remaining issues / risks**
6. **Suggested next task**

## 10. Handling Ambiguity & Scope

- Tier order is **P0 → P1 → P2 → P3** (`PRD.md` §5). Do not start a higher tier until the lower tier's tasks are complete and green, unless told to.
- If a spec detail seems wrong or unsafe, **raise it**; do not "fix" it silently.
- Aadhaar/KYC integration is **mock/sandbox only**. Do not add real UIDAI or third-party KYC calls.
- Do not implement anything listed under "Out of Scope" in `PRD.md` §12.

## 11. Prompt Template (for the human running the agent)

```
CONTEXT
We are building SmartSpace. Read README.md, PRD.md, ARCHITECTURE.md, DESIGN.md, RULES.md, TASKS.md.
Read IMPLEMENTATION.md sections: <list>.

TASK
Implement <TASK-IDs from TASKS.md>.

FILES
Relevant paths: <backend/... , frontend/...>

CONSTRAINTS
- Follow RULES.md and the existing architecture.
- Do not modify unrelated files. Do not add dependencies without recording them.

ACCEPTANCE CRITERIA
<copy from TASKS.md>

TESTING
Add tests. Run lint, typecheck, tests, build. Report using RULES.md §9.
```

## 12. Debugging Prompt Template

```
ERROR
<paste error / stack trace>

EXPECTED BEHAVIOR
<what should happen>

ACTUAL BEHAVIOR
<what happens>

STEPS TO REPRODUCE
1. ...
CONSTRAINT
Do not change the database schema unless the root cause requires it.

Do not modify code yet. Find the root cause and explain:
1. What is failing? 2. Why? 3. Which file? 4. Smallest fix? 5. How to test it?
Then implement the smallest fix, add a regression test, and run the relevant tests.
```
