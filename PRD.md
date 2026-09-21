# Product Requirements Document (PRD)

> **PRD = WHAT + WHY.** For *how*, read `ARCHITECTURE.md`. For the build-level detail, read `IMPLEMENTATION.md`.

## 1. Product

**Name:** SmartSpace — *Smart Space & Resource Availability*
**Tagline:** Book the hall nearby. Enter with trust.
**Category:** Facilities Management
**One-liner:** A web platform that lists small local halls and society spaces for short-duration rental, lets residents discover and book the nearest suitable one, and secures the physical handover on event day through a QR code + Aadhaar-linked identity check run by the hall's watchman.

## 2. Problem

Residential societies, community centres and small halls sit idle for most of the week. Residents who want to host small events (birthday parties, kitty parties, tuition batches, society meetings, get-togethers) cannot easily find an affordable, nearby, right-sized venue.

| # | Problem | Consequence |
|---|---------|-------------|
| P1 | Small halls are undiscoverable — no searchable listing of nearby small-capacity venues | Halls stay idle; residents overpay for oversized banquet halls |
| P2 | Booking is informal (phone calls, registers, word of mouth) | Double-bookings, no digital record, disputes |
| P3 | No reliable way to verify that the person entering on the booked date is the person who booked | Handover relies on the watchman recognising a face or a paper chit → impersonation, unauthorised entry |
| P4 | Renters must separately search for decorators | Time wasted; poor fit between decorator and hall |
| P5 | No visibility for the society into usage, revenue, condition of the hall | No data to price or manage the space; damage/overstay disputes are "he said, she said" |

## 3. Goals & Non-Goals

### Goals (what success looks like)
- G1. A resident can find and book the nearest available small hall in **≤ 5 clicks / ≤ 2 minutes**.
- G2. **Zero double-bookings**, enforced at the database level, with temporary slot locking during payment.
- G3. A watchman can verify an entrant in **≤ 20 seconds** using only a phone.
- G4. Every booking has a complete, auditable lifecycle: `created → paid → QR issued → checked-in → checked-out → rated`.
- G5. Decorator suggestions are matched to the hall's real capacity/layout and the declared event type — not a generic list.
- G6. Hall owners get clear analytics: bookings, occupancy, revenue, ratings.
- G7. Trust is earned only from **verified, check-in-confirmed** bookings.

### Non-Goals (Out of scope for v1)
- Large commercial venues, banquet halls, wedding marketplaces.
- Native mobile apps (a responsive **PWA** covers mobile).
- Full catering / food ordering, sound & lights rental marketplace.
- Real production Aadhaar authentication (a pluggable provider is built; a **mock/sandbox** provider is used in the academic build — see Risks R1).
- Multi-city expansion tooling, multi-currency, franchise management.
- Social feed, chat between residents, gamification.
- Fully automated payouts/settlement to owners (recorded and reported, settled manually in v1).

## 4. Users / Actors

| Role | Description | Primary needs |
|------|-------------|---------------|
| **Resident / Renter** | End user who searches for and books a hall for a personal or society event | Find nearest hall, see real availability, book fast, get a QR, hire a decorator |
| **Society / Hall Owner (Facility Manager)** | Lists the hall; sets availability, pricing and rules | Fill idle slots, control rules, see revenue and usage, trust the renters |
| **Watchman / Security Guard** | Scans the entry QR on event day and verifies the entrant | A dead-simple, fast, big-button screen; works even with poor network |
| **Decorator / Local Vendor** | Independent decoration vendor suggested to and hired by renters | Get relevant leads, manage packages and availability |
| **System Administrator** | Runs the platform | Approve societies/listings, resolve disputes, monitor platform health |

### Personas
- **Priya (Renter, 34, homemaker):** hosts a 25-guest birthday party. Wants a nearby hall for 3 hours this Saturday, under ₹3,000, plus balloon decoration.
- **Mr. Kulkarni (Society secretary, 58):** manages the society hall; not technical. Wants no double-bookings and a simple monthly report.
- **Ramesh (Watchman, 45):** limited English, basic smartphone. Needs green/amber/red clarity, not menus.
- **Sneha (Decorator, 28):** runs a small decoration business. Wants leads matching what she actually offers.

## 5. Scope

### 5.1 MVP (must ship — Tier P0)
1. Authentication + role-based access (Resident, Hall Owner, Watchman, Decorator, Admin).
2. Identity capture (KYC) — one-time, via pluggable provider (mock in dev).
3. Hall listing management (owner) + admin approval.
4. Location-based hall discovery with filters + hall detail page.
5. Availability calendar with **30-minute cell** slot engine and **temporary slot lock**.
6. Booking + payment flow (gateway in test mode) + cancellation/refund policy.
7. Unique signed QR per booking, issued on payment.
8. Watchman scanner (PWA) with QR validation + identity OTP step → check-in.
9. Check-out with condition notes; full audit trail.
10. Ratings (verified only) + trust score.
11. Decorator directory + context-aware matching + enquiry.
12. Owner dashboard (bookings, occupancy, revenue, ratings).
13. Admin panel (approvals, disputes, analytics).
14. Notifications (email/SMS/in-app): confirmation, QR delivery, entry/exit alerts, reminders.

### 5.2 Novelty features from the project document (Tier P1)
| ID | Feature |
|----|---------|
| NV-1 | **QR + Aadhaar dual-layer entry verification** — time-bound signed QR + identity check tied to Aadhaar-linked KYC |
| NV-2 | **Context-aware decorator matching** — hall capacity, layout and declared event type/theme drive suggestions |
| NV-3 | **Two-sided trust score** — built from check-in-confirmed bookings only |
| NV-4 | **Slot-level micro-booking** — 2–4 hour slots, multiple events per day per hall |
| NV-5 | **Watchman-first design** — lightweight scanner UI built for a non-technical guard |
| NV-6 | **Post-event dual accountability log** — check-in/out timestamps + condition notes replace a formal deposit process |

### 5.3 Added novelty features (Tier P2 — added in this spec)
| ID | Feature | Why it matters |
|----|---------|----------------|
| N-01 | **Offline-Resilient Watchman PWA** — signed QR tokens verified on-device; check-ins queue and sync | Society basements/halls have poor signal; entry must never stall |
| N-02 | **Live Capacity Guard** — gate headcount vs declared guests vs hall capacity, with alerts | Safety (overcrowding) and honest usage data |
| N-03 | **Quiet-Hours & Overstay Guard** — society rule engine, wrap-up reminders, overstay detection | Neighbour peace; ends "he refused to leave" disputes |
| N-04 | **Decorator Access Pass + setup/teardown buffers** — scoped, time-boxed QR for the hired decorator | Solves the real gap: decorators need entry *before* the event |
| N-05 | **Resident-Priority Windows & Community Pricing** + **Smart Pricing Advisor** | Hyperlocal trust; fills idle slots using real occupancy data |
| N-06 | **Waitlist & Smart Alternatives** — auto-offer freed slots; suggest nearest free slots/halls | Converts "sorry, taken" into a booking |
| N-07 | **Handover Evidence Pack** — before/after photos, checklist, auto-generated report | Objective dispute resolution without a security deposit |
| N-08 | **Multilingual, low-literacy UI** (English / हिन्दी / मराठी, icon-first watchman UI) | Real users in Indian societies |
| N-09 | **Privacy Dashboard** — consent log, data export, account deletion | DPDP-aligned; builds trust around identity data |

### 5.4 Stretch (Tier P3 — only after P0–P2 are green)
- N-10 **Split-the-Cost** for co-hosts (kitty parties): each co-host pays a share via payment link.
- N-11 **Natural-language event brief** → structured search filters (optional LLM; provider-agnostic).
- N-12 **Web Push notifications** (VAPID).

## 6. Functional Requirements

Priority: **M** = must, **S** = should, **C** = could.

### 6.1 Accounts & Access
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-001 | Users can sign up (email + phone + password) and log in; JWT access + rotating refresh tokens | M |
| FR-002 | Role-based access control for Resident, Hall Owner, Watchman, Decorator, Admin | M |
| FR-003 | Email and phone verification via OTP | M |
| FR-004 | Hall Owner can create Watchman accounts for their hall(s) | M |
| FR-005 | Admin can suspend/reactivate any account | M |
| FR-006 | Users can choose UI language (EN/HI/MR) | S |

### 6.2 Identity / KYC
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-010 | Resident completes one-time identity verification through a pluggable `IdentityProvider` | M |
| FR-011 | System stores **only** provider reference, verified name, masked ID suffix, status and timestamps — **never** a raw Aadhaar number, biometrics, or an Aadhaar photo | M |
| FR-012 | Consent is captured (purpose, version, timestamp) before any KYC call | M |
| FR-013 | Booking is blocked until KYC status = `VERIFIED` | M |
| FR-014 | KYC validity is configurable (default 12 months) and re-verification is prompted on expiry | S |

### 6.3 Hall Listings (Owner/Admin)
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-020 | Owner creates/edits a hall: name, description, address + map pin, capacity (seated/standing), area, layout type, amenities, photos, rules | M |
| FR-021 | Owner sets weekly open hours, blackout dates, slot duration limits (min/max), buffers, price rules (by day/time), cancellation policy | M |
| FR-022 | Owner sets quiet hours and latest end time | S |
| FR-023 | New listings go to `PENDING_APPROVAL`; Admin approves/rejects with reason | M |
| FR-024 | Owner can add society members and give them priority/discount (N-05) | S |

### 6.4 Discovery & Availability
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-030 | Resident searches halls near a location (GPS or typed locality) sorted by distance | M |
| FR-031 | Filters: date, time window, duration, guest count, price range, amenities (AC, parking, kitchen, stage, power backup), rating/trust, indoor/outdoor | M |
| FR-032 | Hall detail page: photos, map, amenities, rules, price rules, trust score, reviews (verified only), availability calendar | M |
| FR-033 | Availability calendar shows free/locked/booked/blocked at 30-minute granularity | M |
| FR-034 | If the requested slot is taken, suggest nearest free slots and nearby halls (N-06) | S |

### 6.5 Booking & Payment
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-040 | Resident selects date + start + duration (min 2h / max 4h default, owner-configurable), event type, title, guest count | M |
| FR-041 | Selecting a slot **locks** it for `SLOT_LOCK_MINUTES` (default 10) — no one else can book it | M |
| FR-042 | Double-booking is impossible (DB-enforced uniqueness on hall × 30-min cell) | M |
| FR-043 | Price shown before payment: base, member discount, platform fee, tax, total | M |
| FR-044 | Payment via gateway (test mode); server verifies signature and webhook; idempotent | M |
| FR-045 | On successful payment, booking → `CONFIRMED` and QR is issued atomically | M |
| FR-046 | Cancellation with policy-based refund; owner cancellation = full refund + trust penalty | M |
| FR-047 | Lock expires automatically if unpaid; slot is released | M |
| FR-048 | Booking history with statuses and downloadable receipt | M |
| FR-049 | Guest count declared at booking and editable until 24h before | S |

### 6.6 QR Entry & Watchman
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-050 | Each confirmed booking gets a unique signed QR valid only in its time window (slot ±grace) | M |
| FR-051 | QR is delivered in-app, by email, and is available offline in the renter's app | M |
| FR-052 | Watchman scans QR; system returns **GO / HOLD / STOP** with reason | M |
| FR-053 | On HOLD (identity step), an OTP goes to the KYC-verified phone; watchman enters the OTP the renter reads out | M |
| FR-054 | Successful verification records `CHECKED_IN`, updates hall live status to *Occupied*, notifies owner and renter | M |
| FR-055 | A QR cannot be reused by another booking, hall, or outside its window; screenshots alone cannot enter (OTP layer) | M |
| FR-056 | Watchman can check-out the booking with checklist, notes and photos | M |
| FR-057 | Watchman scanner works offline for QR validation and queues events for sync (N-01) | S |
| FR-058 | Headcount counter with capacity alerts (N-02) | S |

### 6.7 Post-Event: Accountability, Ratings, Trust
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-060 | Check-in/out timestamps and condition notes stored in an immutable log | M |
| FR-061 | Renter can rate the hall; owner/watchman can rate the renter — **only** for `CHECKED_OUT` bookings | M |
| FR-062 | Trust scores (resident, hall, decorator) computed per the algorithm in `IMPLEMENTATION.md` | M |
| FR-063 | Either party can raise a dispute within 72h of check-out with evidence; Admin resolves | M |
| FR-064 | Handover Evidence Pack PDF generated per booking on demand (N-07) | S |

### 6.8 Decorators
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-070 | Decorator registers, lists packages (event types, themes, capacity range, price, setup time), sets blackout dates; Admin verifies | M |
| FR-071 | Matching engine ranks decorators for a booking using hall capacity, layout, event type/theme, budget, distance, decorator availability, rating | M |
| FR-072 | Every recommendation shows **why it matched** (explainable) | M |
| FR-073 | Renter sends an in-app enquiry; decorator accepts/declines with a quote | M |
| FR-074 | Accepted decorator receives a time-boxed Decorator Access Pass (N-04) | S |

### 6.9 Owner Dashboard & Admin
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-080 | Owner dashboard: bookings, occupancy %, revenue, cancellation rate, ratings, live hall status | M |
| FR-081 | Usage heatmap (weekday × hour) and Smart Pricing Advisor suggestions | S |
| FR-082 | Admin: approve societies/listings, manage users, dispute queue, platform analytics | M |
| FR-083 | Export bookings/revenue as CSV | S |

### 6.10 Notifications
| ID | Requirement | Pri |
|----|-------------|-----|
| FR-090 | Booking confirmation, QR delivery, payment receipt, reminders (T-24h, T-2h), entry/exit alerts, wrap-up and overstay alerts, dispute updates | M |
| FR-091 | Channels: email, SMS (provider abstraction), in-app | M |
| FR-092 | Reliable delivery via outbox with retry | S |

## 7. Non-Functional Requirements

| ID | Category | Requirement |
|----|----------|-------------|
| NFR-01 | Security | HTTPS everywhere; BCrypt/Argon2 password hashing; JWT with short TTL; RBAC on every endpoint; input validation on all inputs |
| NFR-02 | Privacy | No raw Aadhaar number ever stored or logged; PII minimisation; consent log; data export & deletion (aligned with DPDP Act 2023 principles) |
| NFR-03 | Integrity | Slot uniqueness enforced in DB; payment and webhook processing idempotent; append-only audit trail |
| NFR-04 | Performance | Search p95 < 500 ms for 1,000 halls; QR verify p95 < 300 ms online; scanner decision < 1 s offline |
| NFR-05 | Availability | Watchman flow degrades gracefully offline (N-01) |
| NFR-06 | Usability | Mobile-first; WCAG 2.1 AA; watchman screen usable in bright sunlight and with large touch targets |
| NFR-07 | Compatibility | Latest 2 versions of Chrome, Edge, Safari, Firefox; Android 9+/iOS 15+ browsers (camera scan needs HTTPS) |
| NFR-08 | Observability | Structured logs with correlation IDs; health endpoints; basic metrics |
| NFR-09 | Maintainability | Layered architecture; ≥ 70% backend unit-test coverage on domain services; CI green before merge |
| NFR-10 | Localisation | English, Hindi, Marathi; all times shown in Asia/Kolkata; stored in UTC |

## 8. Key User Stories & Acceptance Criteria

### US-01 Discover & book
*As a resident, I want to find the nearest hall free this Saturday 5–8 pm for 25 guests, so that I can host a birthday party.*
- Given I enter a locality/GPS, date, time window and guest count, halls within the radius that fit capacity and are free appear sorted by distance.
- When I pick a slot, it is locked for 10 minutes and a countdown is shown.
- When I pay successfully, I see a confirmation and my QR immediately.
- If the lock expires, the slot returns to available and I am told clearly.

### US-02 Two people, one slot
*As a system, I must never confirm two bookings for the same hall and time.*
- 50 concurrent booking attempts on one slot produce exactly 1 success and 49 `409 SLOT_UNAVAILABLE` responses.

### US-03 Gate verification
*As a watchman, I want to confirm the person at the gate is the verified booker.*
- Scanning a valid QR inside its window shows the booking summary and asks for the OTP the renter received.
- Correct OTP → green **GO**, booking becomes `CHECKED_IN`, owner is notified.
- Screenshot QR on a stranger's phone → the OTP goes to the *booker's* phone, so the stranger cannot pass.
- Expired / wrong hall / cancelled / not-yet-valid QR → red **STOP** with a plain-language reason.

### US-04 Offline gate
*As a watchman with no network, I can still validate today's QR codes.*
- With airplane mode on, a valid QR is recognised as authentic and in-window; the check-in is marked *offline-verified*, queued, and synced when online.

### US-05 Hall owner insight
*As a hall owner, I want to see how the hall is used and earns.*
- Dashboard shows occupancy %, revenue, bookings by status, heatmap and average trust/rating for a chosen period.

### US-06 Decorator match
*As a renter, I want decorators that fit my hall and event.*
- Given hall capacity 60, layout "open hall", event "birthday", theme "balloon/kids", budget ₹5,000, the top results all serve that capacity range, event type and area, are free that day, and each shows a reason like "Fits 40–80 guests · Matches Balloon theme · ₹800 under budget".

### US-07 Fair trust
*As a hall owner, I want ratings that cannot be faked.*
- The rating form is only available for a booking with a recorded check-in and check-out; one rating per side per booking.

### US-08 Dispute
*As an owner, I want proof of overstay or damage.*
- The booking's log shows scheduled end vs actual check-out, checklist results and photos; I can raise a dispute and Admin sees the same evidence.

## 9. Success Criteria (MVP demo — end-to-end)

A user can:
1. Register as a resident, complete mock KYC.
2. Search halls near a location, filter, open a hall.
3. Pick a 3-hour slot, see it locked, pay in test mode.
4. Receive a QR and email confirmation.
5. On event day, the watchman scans the QR, enters the OTP, and the booking is `CHECKED_IN`.
6. Watchman checks the booking out with a checklist.
7. Both sides rate; trust scores update.
8. The owner dashboard reflects the booking and revenue.
9. The renter gets ranked decorator suggestions and sends an enquiry.
10. Admin approves a new hall and resolves a dispute.

### KPIs (for evaluation)
| Metric | Target |
|--------|--------|
| Double-booking rate under concurrency test | 0 |
| Median gate verification time (scan → GO) | ≤ 20 s |
| Screenshot-share impersonation attempts blocked | 100% in test set |
| Offline scan success (valid QR, no network) | ≥ 99% |
| Booking completion (start → paid) in usability test | ≥ 85% |
| Decorator match precision@3 (user rating ≥ 4/5) | ≥ 70% |

## 10. Assumptions
- A1. Halls are small (typical capacity 10–150) and rented for 2–4 hour slots.
- A2. Every hall has, or can assign, one or more watchmen with a smartphone.
- A3. Renters have a smartphone with a working mobile number (for OTP).
- A4. Payments in v1 are collected by the platform and reported to owners (manual settlement).
- A5. Time zone is India Standard Time; currency is INR.

## 11. Risks & Limitations

| ID | Risk | Mitigation |
|----|------|------------|
| R1 | **Aadhaar use is legally regulated.** Real Aadhaar authentication/e-KYC is limited to licensed entities, and private services generally cannot mandate it or store Aadhaar numbers. | Build against an `IdentityProvider` interface; ship a **mock/sandbox** provider for the academic build; support alternates (DigiLocker-style verification, other government ID) behind the same interface; never store raw numbers; seek legal review before any real deployment. |
| R2 | "Same person" can never be proven absolutely (a booker can hand their phone to someone) | The system provides **strong evidence** (QR + OTP to KYC-linked phone + verified name shown to the watchman), and states this limit plainly in the docs and UI copy ("probably the same person"). |
| R3 | Poor connectivity at gate | Offline PWA mode (N-01) |
| R4 | Camera scanning requires HTTPS on phones | Deploy behind TLS even in-house (see `ARCHITECTURE.md` §10) |
| R5 | SMS in India needs sender/template registration (DLT) | SMS provider abstraction; console provider in dev; email + in-app fallback |
| R6 | Rating manipulation by colluding parties | Verified-only ratings, collusion down-weighting, Bayesian smoothing |
| R7 | Scope creep from AI coding tools | Strict tiers (P0→P3), `TASKS.md` gating, `RULES.md` |

## 12. Out of Scope (repeat for the AI)

Do **not** build in v1: native apps, catering marketplace, chat, social feed, gamification, multi-city ops, automated owner payouts, real Aadhaar API integration, AI tutor/chatbot. Do not add libraries or features not listed in `ARCHITECTURE.md` / `IMPLEMENTATION.md` without recording the decision.

## 13. Glossary

| Term | Meaning |
|------|---------|
| **Cell** | A 30-minute atomic unit of a hall's calendar; bookings occupy whole cells |
| **Slot** | A bookable span of consecutive cells (default 2–4 h) |
| **Lock** | Temporary hold on a slot's cells while payment is pending |
| **Buffer** | Cells reserved before/after a booking for setup/cleaning |
| **KYC** | Know Your Customer — one-time identity verification |
| **GO / HOLD / STOP** | Watchman verdicts: enter / needs an extra step / deny |
| **Trust score** | 0–100 reputation computed only from verified, checked-out bookings |
| **Access Pass** | Time-boxed QR credential issued to a decorator for setup/teardown |
