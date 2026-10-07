# Tasks

> Work top to bottom. **One task at a time:** Understand → Plan → Implement → Test → Review → Commit → tick the box → next task.
> Detailed specs live in `IMPLEMENTATION.md` (see the *Refs* on each task). Rules live in `RULES.md`. Tiers: **P0** MVP · **P1** document novelties · **P2** added novelties · **P3** stretch.
> Do not start a phase until the previous phase's **exit criteria** are met and its branch is merged.

**Legend:** `[ ]` todo · `[~]` in progress · `[x]` done · Each task ends with *Done when* acceptance criteria.


## Phase 0 — Project Setup
**Tier:** P0 · **Branch:** `feature/p0-setup`
**Exit criteria:** Repo boots; `docker compose up` gives healthy MySQL + backend + frontend shell; CI green.

- [ ] **TASK-001** Create monorepo structure (`backend/`, `frontend/`, `e2e/`, `infra/`, `docs/`), `.gitignore`, copy spec files to root  
  *Refs:* §2 · *Done when:* Tree matches IMPLEMENTATION §2; `.env`, `*.pem`, `uploads/` git-ignored
- [ ] **TASK-002** Add `.env.example` (no secrets) and document each variable in README  
  *Refs:* §4 · *Done when:* App reads all config from env; nothing hard-coded
- [ ] **TASK-003** Scaffold Spring Boot 3 (Java 21) app with dependencies from §3; package skeleton per ARCHITECTURE §3.1  
  *Refs:* §3 · *Done when:* `./mvnw verify` passes; empty modules present
- [ ] **TASK-004** Scaffold React + TS + Vite + Tailwind 3.4; configure ESLint, Prettier, Vitest, path aliases  
  *Refs:* §3, DESIGN §6 · *Done when:* `npm run lint/typecheck/test/build` pass
- [ ] **TASK-005** Implement Tailwind tokens + fonts (Inter, Noto Sans Devanagari, JetBrains Mono) per DESIGN  
  *Refs:* DESIGN §3-6 · *Done when:* Token classes usable; sample page renders in light/dark
- [ ] **TASK-006** MySQL 8.4 via Docker Compose (+ dev override with MailHog, Adminer); healthchecks  
  *Refs:* §23.2 · *Done when:* `docker compose -f docker-compose.yml -f docker-compose.dev.yml up` healthy
- [ ] **TASK-007** Backend Dockerfile (multi-stage) + frontend Dockerfile + proxy (Caddy/Nginx) config  
  *Refs:* §23 · *Done when:* App reachable via proxy; `/api` proxied; HTTPS locally (internal CA/mkcert)
- [ ] **TASK-008** Flyway wired with `validate` ddl mode; empty `V0__baseline.sql` runs (real schema starts at V1)  
  *Refs:* §5 · *Done when:* Backend starts and reports Flyway success
- [ ] **TASK-009** Actuator health + springdoc OpenAPI + `openapi-typescript` script (`npm run gen:api`)  
  *Refs:* §20, §21.2 · *Done when:* `/actuator/health` UP; `/v3/api-docs` served; types generated
- [ ] **TASK-010** GitHub Actions CI (backend, frontend, secret scan)  
  *Refs:* §23.4 · *Done when:* Pipeline green on a trivial PR
- [ ] **TASK-011** ArchUnit tests for layering rules + `Clock` bean and `Instant.now()` ban  
  *Refs:* ARCH §3.2 · *Done when:* Test fails if a controller calls a repository
- [ ] **TASK-012** Create `docs/DECISIONS.md` and `docs/MEMORY.md` with initial content  
  *Refs:* RULES §8 · *Done when:* Files exist and are updated by each phase

## Phase 1 — Foundations & Core Schema
**Tier:** P0 · **Branch:** `feature/p0-foundations`
**Exit criteria:** Migrations V1–V2 apply cleanly; error handling, security skeleton and base utilities tested.

- [ ] **TASK-013** Migration V1 (users, roles, refresh tokens, OTP challenges, consents, KYC)  
  *Refs:* §5 V1 · *Done when:* Applies on empty DB; Testcontainers test passes
- [ ] **TASK-014** Migration V2 (societies, members, halls, photos, hours, blackouts, price rules, staff)  
  *Refs:* §5 V2 · *Done when:* Applies; CHECK constraints tested
- [ ] **TASK-015** Common module: `ApiError`, exception hierarchy, `@ControllerAdvice`, `traceId` MDC filter  
  *Refs:* §20.1 · *Done when:* All errors match the standard shape
- [ ] **TASK-016** Public-id (UUID) generation, booking-ref generator (collision-safe), paging helpers  
  *Refs:* §5 · *Done when:* Unit tests incl. collision retry
- [ ] **TASK-017** Security config skeleton: stateless, CORS allow-list, security headers  
  *Refs:* §7.4 · *Done when:* Header/CORS tests pass
- [ ] **TASK-018** Bucket4j rate-limit filter with per-endpoint config  
  *Refs:* §7.3 · *Done when:* 429 + Retry-After under load test
- [ ] **TASK-019** File storage service (validate magic bytes, EXIF strip, random name, SHA-256)  
  *Refs:* §7.5 · *Done when:* Rejects fake-extension files; unit tests
- [ ] **TASK-020** Log sanitiser (mask phone/email; never log tokens/OTP)  
  *Refs:* §7.5 · *Done when:* Masking test passes

## Phase 2 — Authentication & RBAC
**Tier:** P0 · **Branch:** `feature/p0-auth`
**Exit criteria:** Users can register, verify, log in, refresh, log out; roles enforced server-side.

- [ ] **TASK-021** Register endpoint with validation, unique email/phone, BCrypt hashing  
  *Refs:* §7.1 · *Done when:* Duplicate email/phone → 409 `ALREADY_EXISTS`
- [ ] **TASK-022** OTP service (email/phone verify, reset): hashed at rest, TTL, attempts, resend cooldown  
  *Refs:* §7.1, ARCH §7 · *Done when:* Unit + integration tests incl. lockout
- [ ] **TASK-023** SMS provider port + `ConsoleSmsProvider`; SMTP email adapter + MailHog  
  *Refs:* §17.2 · *Done when:* Dev OTP visible in console/MailHog
- [ ] **TASK-024** Migration V8 (notification_outbox, notifications, audit_log) + outbox dispatcher (email/SMS/in-app), retry/backoff, dedupe keys  
  *Refs:* §5 V8, §17.1 · *Done when:* Failure → retry → DEAD covered by tests; used by all later phases
- [ ] **TASK-025** Login + JWT access token + refresh cookie (httpOnly/Secure/SameSite=Strict)  
  *Refs:* §7.1 · *Done when:* Cookie flags verified in test
- [ ] **TASK-026** Refresh rotation with reuse detection (revoke family)  
  *Refs:* §7.1 · *Done when:* Reuse test revokes family
- [ ] **TASK-027** Logout, forgot/reset/change password  
  *Refs:* §7.1 · *Done when:* Flows tested end-to-end
- [ ] **TASK-028** Role guard utilities + ownership access helpers (`hallAccess`, `bookingAccess`)  
  *Refs:* §7.2 · *Done when:* Matrix tests: wrong role 403, others' data 404
- [ ] **TASK-029** `GET/PATCH /me`, language preference, profile photo upload  
  *Refs:* §20.2 · *Done when:* Persisted; photo validated
- [ ] **TASK-030** Frontend: auth provider (token in memory), http client with refresh-retry, route guards  
  *Refs:* §21.1-21.2 · *Done when:* 401 → refresh → retry works; no token in storage
- [ ] **TASK-031** Frontend: Login, Register (role choice), Verify OTP, Forgot password screens (i18n keys, all states)  
  *Refs:* DESIGN §12 · *Done when:* Responsive; a11y forms; tests
- [ ] **TASK-032** Watchman account creation by owner + forced password change on first login  
  *Refs:* §7.1 · *Done when:* Watchman cannot self-register

## Phase 3 — KYC & Consent
**Tier:** P0 · **Branch:** `feature/p0-kyc`
**Exit criteria:** A resident can complete mock KYC; no forbidden data is stored anywhere.

- [x] **TASK-033** Consent service + `/kyc/consent`  
  *Refs:* §8.3 · *Done when:* Consent recorded
- [x] **TASK-034** `IdentityProvider` port + `MockIdentityProvider`  
  *Refs:* §8.2 · *Done when:* Mock returns { VERIFIED, 1234 }
- [x] **TASK-035** KYC endpoints: start, complete, status, revoke; assurance level calc  
  *Refs:* §8.3 · *Done when:* `/kyc/status` returns HIGH/STANDARD based on mobile link
- [x] **TASK-036** Schema test asserting no Aadhaar-number/biometric columns; log test asserting no KYC payload logged  
  *Refs:* §8.1 · *Done when:* CI fails if `aadhaar` column added
- [x] **TASK-037** Booking guard `KYC_REQUIRED` (service-level) + KYC expiry job  
  *Refs:* §6.2, §18 · *Done when:* Job schedules warning 14 days prior
- [x] **TASK-038** Frontend: KYC intro (plain-language consent sheet), mock provider page (dev only), status screen  
  *Refs:* DESIGN §7, §13 · *Done when:* Copy per DESIGN §13; all states

## Phase 4 — Societies, Halls & Owner Console
**Tier:** P0 · **Branch:** `feature/p0-halls`
**Exit criteria:** Owner can create a hall through the full wizard; admin can approve; hall visible.

- [ ] **TASK-039** Society CRUD + admin approval + member join/approve  
  *Refs:* §20.2 · *Done when:* Only approved societies can list halls
- [ ] **TASK-040** Hall CRUD with validation (capacity, slot rules, buffers, quiet hours, policies)  
  *Refs:* §5 V2 · *Done when:* Validation + integration tests
- [ ] **TASK-041** Photos upload/reorder/delete  
  *Refs:* §7.5 · *Done when:* Authorised file serving works
- [ ] **TASK-042** Opening hours + blackouts APIs (blackout with existing bookings requires owner-cancel confirmation)  
  *Refs:* §20.2 · *Done when:* Conflict handling tested
- [ ] **TASK-043** Price rules API + rule resolution unit (priority, day mask, time range)  
  *Refs:* §6.5 · *Done when:* Resolver unit tests
- [ ] **TASK-044** Staff (watchman) management for a hall  
  *Refs:* §7.1 · *Done when:* Only owner of that hall can manage
- [ ] **TASK-045** Submit-for-approval + admin approve/reject with reason + audit log + notification  
  *Refs:* §16.1 · *Done when:* Status transitions tested
- [ ] **TASK-046** Frontend: Owner Halls list + creation/edit wizard (Details, Photos, Hours & blackouts, Pricing, Rules, Staff, Submit)  
  *Refs:* §21.3 · *Done when:* All states; responsive; i18n
- [ ] **TASK-047** Frontend: Admin approvals queue (societies, halls)  
  *Refs:* §21.3 · *Done when:* Approve/reject works end-to-end

## Phase 5 — Discovery & Availability Read Model
**Tier:** P0 · **Branch:** `feature/p0-discovery`
**Exit criteria:** Residents find nearest suitable halls with filters and see an accurate calendar.

- [ ] **TASK-048** Migration V10 `localities` + seed + `/geo/localities`  
  *Refs:* §14.3 · *Done when:* Autocomplete returns matches
- [ ] **TASK-049** Haversine + bounding-box search query with filters and window-free check  
  *Refs:* §14.2 · *Done when:* Explain plan uses index; correctness tests near box edges
- [ ] **TASK-050** Search service: post-filters (hours, blackouts, quiet, price, amenities), sorting, ranking  
  *Refs:* §14.2 · *Done when:* Golden-file tests for ranking
- [ ] **TASK-051** Availability read model endpoint (states FREE/LOCKED/BOOKED/BUFFER/CLOSED/PAST)  
  *Refs:* §6.4 · *Done when:* Matches DB state incl. expired locks
- [ ] **TASK-052** Hall detail + reviews endpoints (public summary, trust placeholder)  
  *Refs:* §20.2 · *Done when:* No private data exposed
- [ ] **TASK-053** Frontend: Home, Search (list/map, filter sheet, URL-synced), HallCard, empty/error states  
  *Refs:* DESIGN §10, §21.3 · *Done when:* Works at 375/768/1440
- [ ] **TASK-054** Frontend: Hall detail page + SlotGrid (keyboard, aria labels, legend)  
  *Refs:* DESIGN §7, §21.3 · *Done when:* a11y test passes

## Phase 6 — Slot Engine, Booking & Pricing
**Tier:** P0 · **Branch:** `feature/p0-booking`
**Exit criteria:** Booking creation locks slots atomically; double-booking impossible; lifecycle audited.

- [ ] **TASK-055** Migration V3 (bookings, booking_cells, booking_events, idempotency_keys)  
  *Refs:* §5 V3 · *Done when:* Applies; unique cell constraint verified
- [ ] **TASK-056** `SlotRequest` + cell factory (booked cells + trailing buffer)  
  *Refs:* §6.3 · *Done when:* Unit tests incl. alignment and DST-free IST edge cases
- [ ] **TASK-057** `BookingRulesValidator` implementing all 12 rules with stable error codes  
  *Refs:* §6.2 · *Done when:* One test per rule
- [ ] **TASK-058** `PriceCalculator` (rules, member discount, fee, tax, rounding)  
  *Refs:* §6.5 · *Done when:* Worked example = ₹1,505.39 for 3 h @ ₹450 member 10%
- [ ] **TASK-059** `SlotService.reserve` with JDBC batch insert + lazy expired-lock release  
  *Refs:* §6.3 · *Done when:* Duplicate → `SlotUnavailableException` rolls back booking
- [ ] **TASK-060** **Concurrency test**: 50 threads, one winner  
  *Refs:* §22.2 · *Done when:* Passes 100 consecutive runs
- [ ] **TASK-061** `BookingStateMachine` + `booking_events` writer (transactional)  
  *Refs:* §6.7 · *Done when:* Illegal transitions throw; events exactly-once
- [ ] **TASK-062** `POST /bookings/quote`, `POST /bookings` (Idempotency-Key), `GET /bookings`, `GET /bookings/{id}`  
  *Refs:* §20.2 · *Done when:* Idempotent replay returns same booking
- [ ] **TASK-063** Cancellation service + refund preview + policies  
  *Refs:* §6.6 · *Done when:* Policy × time-band matrix tests
- [ ] **TASK-064** `LockExpiryJob` + lock warnings  
  *Refs:* §18 · *Done when:* Unpaid booking expires and frees cells
- [ ] **TASK-065** Guest-count update (until T-24h) + timeline endpoint  
  *Refs:* §20.2 · *Done when:* Rules enforced
- [ ] **TASK-066** Frontend: booking wizard (slot → details → review), LockCountdown (server-skew aware), PriceBreakdown  
  *Refs:* §21.3 · *Done when:* Refresh-safe; expiry modal
- [ ] **TASK-067** Frontend: My Bookings list/detail with StatusPill + Timeline + cancel dialog with refund preview  
  *Refs:* DESIGN §7 · *Done when:* All states; tests
- [ ] **TASK-068** Slot-taken UX: 409 handling with alternatives placeholder  
  *Refs:* §14.4 · *Done when:* Friendly message + nearby times

## Phase 7 — Payments
**Tier:** P0 · **Branch:** `feature/p0-payments`
**Exit criteria:** Payment success confirms booking and issues QR atomically; safe against replays.

- [ ] **TASK-069** Migration V4 (payments, refunds)  
  *Refs:* §5 V4 · *Done when:* Applies
- [ ] **TASK-070** `PaymentGateway` port + `MockGateway` + `RazorpayGateway` (test mode)  
  *Refs:* ARCH §1 · *Done when:* Contract tests shared by both adapters
- [ ] **TASK-071** `POST /payments/orders` (amount must equal booking total; idempotent)  
  *Refs:* §20.3 · *Done when:* Amount mismatch → `AMOUNT_MISMATCH`
- [ ] **TASK-072** `POST /payments/verify` with constant-time HMAC check; confirm booking in one transaction  
  *Refs:* §20.3 · *Done when:* Booking CONFIRMED + PAID + QR_ISSUED events
- [ ] **TASK-073** Webhook endpoint (raw-body HMAC, idempotent, safety net)  
  *Refs:* §20.3 · *Done when:* Duplicate webhook is a no-op
- [ ] **TASK-074** Late-payment race handling (expired booking → auto refund)  
  *Refs:* §6.3 · *Done when:* Race test passes
- [ ] **TASK-075** Refund pipeline (refund row → gateway → status → event → notification)  
  *Refs:* §6.6 · *Done when:* Refund recorded and shown to user
- [ ] **TASK-076** Receipt PDF  
  *Refs:* §20.2 · *Done when:* PDF has correct totals
- [ ] **TASK-077** Frontend: checkout page (Razorpay lazy-loaded / MockCheckout), success + failure states  
  *Refs:* §21.3 · *Done when:* E2E-01 payment step passes

## Phase 8 — QR Credentials
**Tier:** P0 · **Branch:** `feature/p0-qr`
**Exit criteria:** Every confirmed booking has a signed, time-windowed, revocable QR.

- [ ] **TASK-078** Migration V5 (entry_credentials, entry_logs, hall_live_status)  
  *Refs:* §5 V5 · *Done when:* Applies
- [ ] **TASK-079** Ed25519 key loading + `gen-qr-keys.sh`; `/entry/public-keys` (raw 32-byte key)  
  *Refs:* §4.3, §9.4 · *Done when:* Key extraction test
- [ ] **TASK-080** `QrTokenService` issue/verify per §9.1–9.3 with all reason codes  
  *Refs:* §9 · *Done when:* Unit tests for each failure code + tamper tests
- [ ] **TASK-081** Issue HOLDER credential inside payment-confirm transaction  
  *Refs:* §9.2 · *Done when:* Atomic with confirmation
- [ ] **TASK-082** Revocation on cancel/expiry/KYC revoke  
  *Refs:* §9.5 · *Done when:* Revoked QR rejected
- [ ] **TASK-083** `GET /bookings/{id}/qr` + `QR_READY` notification with inline QR PNG  
  *Refs:* §17.3 · *Done when:* Email shows QR in MailHog
- [ ] **TASK-084** Frontend: QrCard + booking QR screen (cached by service worker), brightness hint  
  *Refs:* §21.3 · *Done when:* Works offline after first load

## Phase 9 — Entry & Watchman (Core)
**Tier:** P0 · **Branch:** `feature/p0-entry`
**Exit criteria:** Watchman scans, verifies OTP, checks people in; owner sees live status.

- [ ] **TASK-085** OTP gate challenge service (create/verify, hashed, 3 attempts, 180 s)  
  *Refs:* §10.1, §7 · *Done when:* Lockout tests
- [ ] **TASK-086** `POST /entry/scan` implementing ordered checks and verdicts (GO/HOLD/STOP)  
  *Refs:* §10.1 · *Done when:* Table-driven tests over all reason codes
- [ ] **TASK-087** `POST /entry/verify-otp` → CHECK_IN transition + live status + notifications  
  *Refs:* §10.1 · *Done when:* Booking CHECKED_IN; owner notified
- [ ] **TASK-088** Re-entry flow  
  *Refs:* §10.1 · *Done when:* Logged without OTP
- [ ] **TASK-089** `GET /entry/today` + staff-hall authorisation (`WRONG_HALL`)  
  *Refs:* §9.3 · *Done when:* Other-hall watchman blocked
- [ ] **TASK-090** Frontend: watchman layout (no menus), WatchHome, online/offline pill  
  *Refs:* DESIGN §9 · *Done when:* Large targets; i18n
- [ ] **TASK-091** Frontend: Scanner (`html5-qrcode`, torch, manual fallback, wake lock)  
  *Refs:* §21.3 · *Done when:* Works on Android Chrome & iOS Safari over HTTPS
- [ ] **TASK-092** Frontend: Verdict screens GO/HOLD/STOP + OtpPad + beep/vibrate  
  *Refs:* DESIGN §9 · *Done when:* Snapshot + interaction tests
- [ ] **TASK-093** Owner live-status widget  
  *Refs:* §15.1 · *Done when:* Flips to Occupied on check-in
- [ ] **TASK-094** E2E-04 and E2E-05 (valid flow; screenshot-share fails)  
  *Refs:* §22.3 · *Done when:* Both pass

## Phase 10 — Check-out, Handover & Audit Trail
**Tier:** P0 · **Branch:** `feature/p0-checkout`
**Exit criteria:** Bookings can be checked out with evidence; full lifecycle visible.

- [ ] **TASK-095** Migration V6 (handover_reports/photos **and** ratings, trust_scores, disputes, dispute_evidence — one migration; ratings/disputes used from Phase 11/13)  
  *Refs:* §5 V6 · *Done when:* Applies; never edited later
- [ ] **TASK-096** `POST /entry/checkout` (checklist, notes, photos, overstay calc, rating window)  
  *Refs:* §10.2 · *Done when:* CHECKED_OUT + event; hall → CLEANING → FREE
- [ ] **TASK-097** Optional BEFORE report endpoint  
  *Refs:* §11.2 · *Done when:* Stored; shown in timeline
- [ ] **TASK-098** `NoShowJob`, `HallStatusJob`  
  *Refs:* §18 · *Done when:* Idempotent; tests
- [ ] **TASK-099** Frontend: CheckoutForm (6 checklist rows, notes, ≤4 camera photos, Finish)  
  *Refs:* DESIGN §9 · *Done when:* Works on mobile
- [ ] **TASK-100** Frontend: Timeline component driven by `booking_events`  
  *Refs:* DESIGN §7 · *Done when:* Shows created→paid→QR→in→out→rated

## Phase 11 — Ratings & Trust
**Tier:** P0 · **Branch:** `feature/p0-trust`
**Exit criteria:** Verified-only ratings; explainable two-sided trust scores.

- [ ] **TASK-101** Rating/trust entities and repositories on the V6 tables (no new migration)  
  *Refs:* §5 V6 · *Done when:* Mappings validated by Hibernate `validate`
- [ ] **TASK-102** Rating eligibility rules + `POST /bookings/{id}/ratings`  
  *Refs:* §12.1 · *Done when:* Each rule tested (no check-in → blocked)
- [ ] **TASK-103** Trust calculators (resident, hall, decorator) with decay, Bayesian prior, penalties, badges  
  *Refs:* §12.3-12.5 · *Done when:* Worked examples reproduce **72.6** and **73.9**
- [ ] **TASK-104** Collusion down-weight + admin flag list  
  *Refs:* §12.2 · *Done when:* Test with repeated pair
- [ ] **TASK-105** `TrustRecomputeJob` + on-event recompute + `components` JSON  
  *Refs:* §12.6 · *Done when:* Idempotent
- [ ] **TASK-106** Frontend: rating forms per side, TrustBadge, 'Why this score?' drawer  
  *Refs:* DESIGN §7 · *Done when:* Plain-language explanation
- [ ] **TASK-107** Show verified reviews + trust on hall detail and search cards  
  *Refs:* §21.3 · *Done when:* Only verified ratings appear

## Phase 12 — Decorators & Matching (NV-2)
**Tier:** P0 · **Branch:** `feature/p0-decorators`
**Exit criteria:** Renter gets ranked, explained decorator matches and can enquire.

- [x] **TASK-108** Migration V7 (decorators, packages, blackouts, enquiries)  
  *Refs:* §5 V7 · *Done when:* Applies
- [x] **TASK-109** Decorator profile/package/blackout APIs + admin verification  
  *Refs:* §13.1-13.2 · *Done when:* Only approved appear
- [x] **TASK-110** `MatchScorer` + hard filters F1–F5 + weights config  
  *Refs:* §13.4 · *Done when:* Worked example = **93**; weights sum to 1.0
- [x] **TASK-111** Explanations/warnings generator (localised templates)  
  *Refs:* §13.4 · *Done when:* Each factor produces expected text
- [x] **TASK-112** Match endpoints (booking-level and hall-level)  
  *Refs:* §13.3 · *Done when:* Top-N sorted; best package per decorator
- [x] **TASK-113** Enquiry workflow (send → respond → confirm) + expiry job  
  *Refs:* §13.5 · *Done when:* State transitions tested
- [x] **TASK-114** Frontend: Decorator directory/profile, DecoratorMatchCard, budget/theme pickers, enquiry UI  
  *Refs:* DESIGN §7 · *Done when:* Reasons visible on every card
- [x] **TASK-115** Frontend: Decorator console (profile, packages, availability, enquiry inbox + quote)  
  *Refs:* §21.3 · *Done when:* All states
- [ ] **TASK-116** E2E-08 (without pass step)  
  *Refs:* §22.3 · *Done when:* Passes

## Phase 13 — Owner Analytics, Admin, Disputes & Notifications
**Tier:** P0 · **Branch:** `feature/p0-ops`
**Exit criteria:** Owner and admin have the visibility promised in the PRD; notifications reliable.

- [ ] **TASK-117** Template catalogue (en) for all P0 codes; `ReminderJob`  
  *Refs:* §17.3 · *Done when:* Reminders sent once
- [x] **TASK-118** In-app notifications API + bell/unread UI  
  *Refs:* §20.2 · *Done when:* Mark read works
- [x] **TASK-119** Owner analytics: summary, revenue series, CSV export  
  *Refs:* §15.1 · *Done when:* Numbers verified against seed
- [x] **TASK-120** Usage heatmap endpoint + chart  
  *Refs:* §15.2 · *Done when:* Matches seeded pattern
- [x] **TASK-121** Disputes: raise, evidence, admin resolve (trust penalty hook, refund option)  
  *Refs:* §16.2 · *Done when:* 72 h window enforced
- [x] **TASK-122** Admin: users, suspend/reactivate, revoke KYC, platform analytics, job health  
  *Refs:* §16.1 · *Done when:* All actions audited
- [x] **TASK-123** Frontend: Owner dashboard & bookings, Admin console pages  
  *Refs:* DESIGN §10 · *Done when:* Responsive; all states
- [ ] **TASK-124** E2E-06, E2E-07, E2E-10, E2E-11  
  *Refs:* §22.3 · *Done when:* All pass

## Phase 14 — P1 Hardening (verify the document novelties)
**Tier:** P1 · **Branch:** `feature/p1-hardening`
**Exit criteria:** NV-1…NV-6 demonstrably work and are documented with evidence.

- [ ] **TASK-125** NV-1 acceptance run: QR-only vs QR+OTP attack scenarios recorded  
  *Refs:* §25.1 · *Done when:* Report table committed to docs/
- [ ] **TASK-126** NV-4 micro-slot checks: several bookings per day per hall with buffers  
  *Refs:* §6.3 · *Done when:* Calendar view verified
- [ ] **TASK-127** Security review pass (authz matrix, IDOR, rate limits, headers, logs, uploads)  
  *Refs:* §7, §22.2 · *Done when:* Checklist signed off in docs/MEMORY.md
- [ ] **TASK-128** Performance smoke: search p95, scan p95  
  *Refs:* PRD NFR-04 · *Done when:* Meets targets on seed data
- [ ] **TASK-129** Accessibility pass (axe) on core pages  
  *Refs:* DESIGN §11 · *Done when:* Zero high-severity issues
- [ ] **TASK-130** Seed data + `POST /dev/reset-demo` + clock fast-forward (dev only)  
  *Refs:* §24.1 · *Done when:* Demo repeatable in one command

## Phase 15 — N-01 Offline-Resilient Watchman PWA
**Tier:** P2 · **Branch:** `feature/p2-offline`
**Exit criteria:** Gate works with no network; syncs safely.

- [ ] **TASK-131** Manifest endpoint with revoked list and key set  
  *Refs:* §10.4 · *Done when:* No PII in payload (test)
- [ ] **TASK-132** PWA setup (`injectManifest`), runtime caching rules, install prompt  
  *Refs:* §21.4 · *Done when:* Lighthouse PWA installable
- [ ] **TASK-133** IndexedDB store + `lib/qr-verify.ts` offline verifier  
  *Refs:* §21.5 · *Done when:* Boundary-time unit tests
- [ ] **TASK-134** Offline identity step (compare with ID) + `MANUAL_OFFLINE` logging  
  *Refs:* §10.4 · *Done when:* Tagged in logs and owner notice
- [ ] **TASK-135** Outbox queue + background sync + `POST /entry/sync` (idempotent, conflict handling)  
  *Refs:* §10.4 · *Done when:* Duplicate/conflict tests
- [ ] **TASK-136** UI: offline banner, queue pill, synced toast  
  *Refs:* DESIGN §9 · *Done when:* Manual airplane-mode test documented
- [ ] **TASK-137** E2E-09  
  *Refs:* §22.3 · *Done when:* Passes

## Phase 16 — N-02 Capacity Guard & N-03 Quiet/Overstay Guard
**Tier:** P2 · **Branch:** `feature/p2-guards`
**Exit criteria:** Safety and neighbour-peace features live.

- [ ] **TASK-138** Headcount API + level engine + alert dedupe + logs  
  *Refs:* §19 N-02 · *Done when:* Level change fires exactly one alert
- [ ] **TASK-139** Headcount stepper UI with live 'n / max' and colour levels  
  *Refs:* DESIGN §9 · *Done when:* Accessible; large targets
- [ ] **TASK-140** Owner analytics: declared vs peak vs capacity  
  *Refs:* §15.1 · *Done when:* Chart present
- [ ] **TASK-141** `WrapUpOverstayJob` + `WRAP_UP_15`/`SLOT_ENDED`/`OVERSTAY` notifications  
  *Refs:* §18 · *Done when:* Timings tested with fixed clock
- [ ] **TASK-142** Overstay minutes/fee in check-out + evidence data  
  *Refs:* §19 N-03 · *Done when:* Fee = per-15-min × ceil
- [ ] **TASK-143** Quiet-hours banners on hall page and checkout  
  *Refs:* §19 N-03 · *Done when:* Shown when configured

## Phase 17 — N-04 Decorator Pass & N-07 Evidence Pack
**Tier:** P2 · **Branch:** `feature/p2-pass-evidence`
**Exit criteria:** Decorators can enter for setup; disputes have objective evidence.

- [x] **TASK-144** SETUP cell reservation on confirm + fallback 'setup inside slot'  
  *Refs:* §13.5 · *Done when:* Conflict case handled
- [x] **TASK-145** DECORATOR credential issue/revoke + pass endpoint + vendor pass screen  
  *Refs:* §13.6 · *Done when:* Pass valid only in window
- [x] **TASK-146** Watchman DECORATOR verdict screen + DECORATOR_IN/OUT logs + notifications  
  *Refs:* §13.6 · *Done when:* E2E-08 full path passes
- [ ] **TASK-147** Optional renter approval for decorator entry  
  *Refs:* §13.6 · *Done when:* HOLD until approved / timeout
- [ ] **TASK-148** Evidence PDF generator (timeline, scheduled vs actual, checklist diff, photos + hashes)  
  *Refs:* §11.4 · *Done when:* Golden-content test; access-controlled
- [ ] **TASK-149** BEFORE-photo capture at check-in (optional UI)  
  *Refs:* §11.2 · *Done when:* Photos stored with hash

## Phase 18 — N-05 Community Pricing/Advisor & N-06 Waitlist/Alternatives
**Tier:** P2 · **Branch:** `feature/p2-community`
**Exit criteria:** Hyperlocal priority, smart pricing, and slot recovery live.

- [ ] **TASK-150** Member priority windows + member discount in validator/pricing + owner member UI  
  *Refs:* §6.8 · *Done when:* Member can book beyond public window
- [ ] **TASK-151** Migration V9 (waitlist, pricing_suggestions, cohosts)  
  *Refs:* §5 V9 · *Done when:* Applies
- [ ] **TASK-152** `PricingAdvisorJob` + suggestions API + Apply/Dismiss UI  
  *Refs:* §15.3 · *Done when:* Rules generated only after Apply
- [ ] **TASK-153** Alternatives service + 409 payload + UI chips  
  *Refs:* §14.4 · *Done when:* Returns ≥ 1 alternative when any exists
- [ ] **TASK-154** Waitlist API + promotion on expiry/cancel + 15-min offer lock  
  *Refs:* §19 N-06 · *Done when:* FIFO order; chain to next on expiry
- [ ] **TASK-155** Frontend: waitlist button/list, offer banner  
  *Refs:* §21.3 · *Done when:* All states

## Phase 19 — N-08 Multilingual & N-09 Privacy Dashboard
**Tier:** P2 · **Branch:** `feature/p2-i18n-privacy`
**Exit criteria:** App usable in EN/HI/MR; users control their data.

- [ ] **TASK-156** Complete `hi` and `mr` translations for all namespaces + CI key-coverage check  
  *Refs:* §19 N-08 · *Done when:* No missing keys
- [ ] **TASK-157** Localised email/SMS templates (en/hi/mr)  
  *Refs:* §17 · *Done when:* Rendered per user language
- [ ] **TASK-158** Error-code → localised message mapping in UI  
  *Refs:* §19 N-08 · *Done when:* All codes covered
- [ ] **TASK-159** Watchman spoken verdict (speechSynthesis) with silent fallback  
  *Refs:* §19 N-08 · *Done when:* Toggle in settings
- [ ] **TASK-160** Privacy APIs: overview, consent revoke, export, delete  
  *Refs:* §19 N-09 · *Done when:* Export has no other users' data
- [ ] **TASK-161** Frontend: Privacy dashboard with retention table  
  *Refs:* DESIGN §13 · *Done when:* Clear language
- [ ] **TASK-162** E2E-12 (languages, viewports, axe)  
  *Refs:* §22.3 · *Done when:* Passes

## Phase 20 — P3 Stretch (only if all above is green)
**Tier:** P3 · **Branch:** `feature/p3-stretch`
**Exit criteria:** Optional extras; must not destabilise the core.

- [ ] **TASK-163** N-10 Split-the-Cost  
  *Refs:* §19 N-10 · *Done when:* Pay-link flow + auto refund on failure
- [ ] **TASK-164** N-11 Natural-language brief → editable filter chips (rule-based default)  
  *Refs:* §19 N-11 · *Done when:* No text stored
- [ ] **TASK-165** N-12 Web Push (VAPID)  
  *Refs:* §19 N-12 · *Done when:* Push received for wrap-up alert

## Phase 21 — Release & Handover
**Tier:** P0 · **Branch:** `release/1.0`
**Exit criteria:** Demo-ready, documented, deployable.

- [ ] **TASK-166** Run all E2E journeys and fix flakiness  
  *Refs:* §22.3 · *Done when:* All E2E green in CI
- [ ] **TASK-167** Coverage/quality gates and dependency + secret scans  
  *Refs:* §22.4 · *Done when:* Gates met
- [ ] **TASK-168** Production compose hardening (secrets, TLS, backups, restore drill)  
  *Refs:* §23 · *Done when:* Restore tested once
- [ ] **TASK-169** README final: run, test, deploy, demo accounts, troubleshooting  
  *Refs:* README · *Done when:* Fresh clone → running app by README only
- [ ] **TASK-170** Export final OpenAPI to `docs/api/openapi.json`; update ARCHITECTURE/DECISIONS to match reality  
  *Refs:* RULES §8 · *Done when:* Docs match code
- [ ] **TASK-171** Demo rehearsal following §24.2 + evaluation runs from §25.1  
  *Refs:* §24, §25 · *Done when:* Results tables committed to docs/

---

## Task Index

| Phase | Tasks |
|-------|-------|
| Phase 0 — Project Setup | TASK-001 – TASK-012 |
| Phase 1 — Foundations & Core Schema | TASK-013 – TASK-020 |
| Phase 2 — Authentication & RBAC | TASK-021 – TASK-032 |
| Phase 3 — KYC & Consent | TASK-033 – TASK-038 |
| Phase 4 — Societies, Halls & Owner Console | TASK-039 – TASK-047 |
| Phase 5 — Discovery & Availability Read Model | TASK-048 – TASK-054 |
| Phase 6 — Slot Engine, Booking & Pricing | TASK-055 – TASK-068 |
| Phase 7 — Payments | TASK-069 – TASK-077 |
| Phase 8 — QR Credentials | TASK-078 – TASK-084 |
| Phase 9 — Entry & Watchman (Core) | TASK-085 – TASK-094 |
| Phase 10 — Check-out, Handover & Audit Trail | TASK-095 – TASK-100 |
| Phase 11 — Ratings & Trust | TASK-101 – TASK-107 |
| Phase 12 — Decorators & Matching (NV-2) | TASK-108 – TASK-116 |
| Phase 13 — Owner Analytics, Admin, Disputes & Notifications | TASK-117 – TASK-124 |
| Phase 14 — P1 Hardening (verify the document novelties) | TASK-125 – TASK-130 |
| Phase 15 — N-01 Offline-Resilient Watchman PWA | TASK-131 – TASK-137 |
| Phase 16 — N-02 Capacity Guard & N-03 Quiet/Overstay Guard | TASK-138 – TASK-143 |
| Phase 17 — N-04 Decorator Pass & N-07 Evidence Pack | TASK-144 – TASK-149 |
| Phase 18 — N-05 Community Pricing/Advisor & N-06 Waitlist/Alternatives | TASK-150 – TASK-155 |
| Phase 19 — N-08 Multilingual & N-09 Privacy Dashboard | TASK-156 – TASK-162 |
| Phase 20 — P3 Stretch (only if all above is green) | TASK-163 – TASK-165 |
| Phase 21 — Release & Handover | TASK-166 – TASK-171 |

**Total:** 171 tasks.

## Per-Task Loop (from the vibe-coding workflow)

```
1. READ  2. UNDERSTAND  3. PLAN  4. IMPLEMENT  5. TEST  6. REVIEW  7. FIX  8. COMMIT  9. UPDATE DOCS (TASKS.md, docs/MEMORY.md)
```

## Notes / Scope Changes
_(The agent appends any approved scope change or deviation here, with date and reason.)_
