# SmartSpace — Detailed Implementation Guide

> **Audience:** the AI coding agent (Antigravity) and the human supervising it.
> **Companion files:** `README.md`, `PRD.md`, `ARCHITECTURE.md`, `DESIGN.md`, `RULES.md`, `TASKS.md`.
> **How to read:** build in the order of `TASKS.md`. Each task cites the sections here it needs. This file is the *source of truth for details* (schema, algorithms, API, screens, tests). Where it disagrees with the other files, `ARCHITECTURE.md` decisions win; report the conflict.

## Table of Contents
0. How to drive the build (kickoff & phase prompts)
1. Scope tiers & novelty map
2. Repository layout
3. Versions & dependencies
4. Configuration & environment
5. Database schema (Flyway)
6. Core domain rules (slot engine, pricing, cancellation, state machine)
7. Security implementation
8. KYC / identity module
9. QR token module
10. Entry, watchman & offline mode
11. Check-out & Handover Evidence Pack
12. Ratings & trust score
13. Decorator module & matching engine
14. Discovery & search
15. Owner dashboard, analytics & Smart Pricing Advisor
16. Admin panel & disputes
17. Notifications
18. Scheduled jobs
19. Added novelty features (N-01 … N-12) — build specs
20. REST API contract
21. Frontend implementation
22. Testing strategy
23. DevOps
24. Seed data & demo script
25. Evaluation plan, risks, future work

---

## 0. How to Drive the Build

### 0.1 Kickoff prompt (paste once into the agent)

```
You are building SmartSpace, a small-hall discovery and verified-entry booking platform.
Read, in order: README.md, PRD.md, ARCHITECTURE.md, DESIGN.md, RULES.md, TASKS.md, IMPLEMENTATION.md.
Do NOT write code yet. Then:
1. Summarise the product, the fixed stack, and the tier plan (P0-P3) in 10 lines.
2. List any ambiguities or missing information.
3. Produce the implementation plan for Phase 0 (TASK-001 to TASK-012) as a plan artifact.
Wait for my approval before making changes.
```

### 0.2 Phase prompt template
Use the template in `RULES.md` §11. One phase = one branch (`feature/p0-<slug>`). Merge only when the phase's exit criteria in `TASKS.md` pass.

### 0.3 Recommended agent workflow inside the IDE
- Ask for a **plan artifact first** for any phase; review; then approve execution.
- Keep the backend and frontend as separate agent tasks; sync via the OpenAPI spec (`/v3/api-docs`) and generate TS types with `openapi-typescript`.
- After each phase ask the agent to update `docs/MEMORY.md` and tick `TASKS.md`.
- If you place `RULES.md` as a workspace rule in the IDE's rules settings, the agent applies it to every task (the exact location of the rules setting may vary by IDE version — check its settings).

---

## 1. Scope Tiers & Novelty Map

| Tier | Contents | Phases in `TASKS.md` |
|------|----------|----------------------|
| **P0 – MVP** | Auth/RBAC, KYC (mock), halls, discovery, slot engine + lock, booking + payment, QR, watchman scan + OTP + check-in/out, ratings + trust, decorators + matching, owner dashboard, admin, notifications | Phases 0–13 |
| **P1 – Document novelties** | NV-1 … NV-6 (dual-layer entry, contextual matching, two-sided trust, micro-slots, watchman-first, accountability log) | Built inside P0 modules, hardened in Phase 14 |
| **P2 – Added novelties** | N-01 … N-09 | Phases 15–19 |
| **P3 – Stretch** | N-10 … N-12 | Phase 20 |

Novelty → module map:

| Novelty | Module(s) | Section |
|---------|-----------|---------|
| NV-1 QR + Aadhaar dual layer | `kyc`, `entry` | §8, §9, §10 |
| NV-2 Context-aware decorator matching | `decorator` | §13 |
| NV-3 Two-sided trust | `rating` | §12 |
| NV-4 Micro-booking | `availability`, `booking` | §6 |
| NV-5 Watchman-first | `frontend/features/watchman` | §10, §21 |
| NV-6 Dual accountability log | `entry`, `handover` | §10, §11 |
| N-01 … N-12 | various | §19 |

---

## 2. Repository Layout

```
smart-space/
├── README.md  PRD.md  ARCHITECTURE.md  DESIGN.md  RULES.md  TASKS.md  IMPLEMENTATION.md
├── .env.example  .gitignore  docker-compose.yml  docker-compose.dev.yml
├── docs/
│   ├── DECISIONS.md          # ADRs (create on first deviation/dependency)
│   ├── MEMORY.md             # current status for the agent (create in Phase 0)
│   └── api/                  # exported openapi.json
├── backend/
│   ├── pom.xml  mvnw  Dockerfile
│   └── src/
│       ├── main/java/com/smartspace/...        # see ARCHITECTURE.md §3.1
│       ├── main/resources/
│       │   ├── application.yml  application-dev.yml  application-prod.yml
│       │   ├── db/migration/V1__... .sql
│       │   ├── db/seed/  (dev-only seed: R__seed_dev.sql, loaded by dev profile)
│       │   ├── i18n/  messages_en.properties  messages_hi.properties  messages_mr.properties
│       │   └── templates/  email/*.html  pdf/*.html
│       └── test/java/com/smartspace/...
├── frontend/
│   ├── package.json  vite.config.ts  tailwind.config.ts  tsconfig.json  Dockerfile  nginx.conf
│   ├── public/  (icons, manifest icons)
│   └── src/  (see ARCHITECTURE.md §4)
├── e2e/                      # Playwright
│   ├── playwright.config.ts
│   └── tests/
├── infra/
│   ├── caddy/Caddyfile  (or nginx/)
│   ├── scripts/  backup.sh  restore.sh  gen-qr-keys.sh
│   └── mysql/init.sql
└── .github/workflows/ci.yml
```

---

## 3. Versions & Dependencies

| Item | Version guidance |
|------|------------------|
| Java | 21 (LTS) |
| Spring Boot | 3.x (3.3 or newer stable 3.x line; pin the exact version in `pom.xml`) |
| MySQL | 8.4 LTS |
| Node | 20 or 22 LTS |
| React | 18.x; TypeScript 5.x; Vite 5+; Tailwind CSS **3.4** |
| Playwright | latest stable |

### Backend (`pom.xml` dependencies)
`spring-boot-starter-web`, `-security`, `-data-jpa`, `-validation`, `-actuator`, `-mail`, `-thymeleaf` (email templates), `mysql-connector-j`, `flyway-core` + `flyway-mysql`, `io.jsonwebtoken:jjwt-api/impl/jackson` (0.12.x), `com.google.zxing:core` + `javase`, `com.bucket4j:bucket4j-core`, `org.springdoc:springdoc-openapi-starter-webmvc-ui`, `org.mapstruct:mapstruct` (+ processor), `org.projectlombok:lombok`, `com.github.librepdf:openpdf`, `micrometer-registry-prometheus`, `com.google.guava:guava` (optional, small helpers).
**Test:** `spring-boot-starter-test`, `spring-security-test`, `org.testcontainers:mysql` + `junit-jupiter`, `com.tngtech.archunit:archunit-junit5`, `awaitility`, `greenmail` (email tests).

### Frontend (`package.json`)
Runtime: `react`, `react-dom`, `react-router-dom`, `@tanstack/react-query`, `react-hook-form`, `zod`, `@hookform/resolvers`, `i18next`, `react-i18next`, `leaflet`, `react-leaflet`, `html5-qrcode`, `qrcode.react`, `idb`, `@noble/curves`, `recharts`, `lucide-react`, `date-fns`, `date-fns-tz`, `clsx`, `@fontsource/inter`, `@fontsource/noto-sans-devanagari`, `@fontsource/jetbrains-mono`.
Dev: `vite`, `@vitejs/plugin-react`, `vite-plugin-pwa`, `typescript`, `tailwindcss`, `postcss`, `autoprefixer`, `eslint`, `prettier`, `vitest`, `@testing-library/react`, `@testing-library/user-event`, `jsdom`, `openapi-typescript`, `msw`, `@playwright/test`, `@axe-core/playwright`.

---

## 4. Configuration & Environment

### 4.1 `application.yml` (keys; values come from env)

```yaml
spring:
  application.name: smartspace
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate.ddl-auto: validate
    open-in-view: false
    properties.hibernate.jdbc.time_zone: UTC
  flyway.enabled: true
  servlet.multipart.max-file-size: ${MAX_UPLOAD_MB:5}MB
  mail:
    host: ${SMTP_HOST}
    port: ${SMTP_PORT}
    username: ${SMTP_USER:}
    password: ${SMTP_PASSWORD:}
server:
  port: 8080
  forward-headers-strategy: framework
app:
  timezone: ${APP_TIMEZONE:Asia/Kolkata}
  base-url: ${APP_BASE_URL}
  cors.allowed-origins: ${CORS_ALLOWED_ORIGINS}
  jwt:
    secret: ${JWT_SECRET}
    access-ttl-minutes: ${JWT_ACCESS_TTL_MIN:15}
    refresh-ttl-days: ${JWT_REFRESH_TTL_DAYS:7}
  booking:
    cell-minutes: 30
    lock-minutes: ${SLOT_LOCK_MINUTES:10}
    min-lead-minutes: 120
    default-min-slot-minutes: 120
    default-max-slot-minutes: 240
    platform-fee-percent: ${PLATFORM_FEE_PERCENT:5}
    tax-percent: ${TAX_PERCENT:18}
    no-show-grace-minutes: 30
  qr:
    private-key-path: ${QR_PRIVATE_KEY_PATH}
    public-key-path: ${QR_PUBLIC_KEY_PATH}
    key-id: ${QR_KEY_ID:k1}
    early-arrival-minutes: 30
    exit-grace-minutes: 15
  otp:
    ttl-seconds: ${OTP_TTL_SECONDS:180}
    max-attempts: ${OTP_MAX_ATTEMPTS:3}
    resend-cooldown-seconds: 30
  kyc:
    provider: ${KYC_PROVIDER:mock}
    validity-months: 12
  payment:
    provider: ${PAYMENT_PROVIDER:mock}      # mock | razorpay
    razorpay.key-id: ${RAZORPAY_KEY_ID:}
    razorpay.key-secret: ${RAZORPAY_KEY_SECRET:}
    razorpay.webhook-secret: ${RAZORPAY_WEBHOOK_SECRET:}
  sms.provider: ${SMS_PROVIDER:console}
  storage.path: ${FILE_STORAGE_PATH:./uploads}
  security.pii-encryption-key: ${PII_ENCRYPTION_KEY_B64}
  rate-limit:
    login-per-minute: 10
    otp-send-per-hour: 6
    scan-per-minute: 60
```

### 4.2 `.env.example`
Provided at repo root (see file). **Never commit a real `.env`.**

### 4.3 QR signing keys
```
# infra/scripts/gen-qr-keys.sh
openssl genpkey -algorithm ed25519 -out qr_private.pem
openssl pkey -in qr_private.pem -pubout -out qr_public.pem
```
Java loads them with `KeyFactory.getInstance("Ed25519")` (PKCS#8 for private, X.509 for public). The raw 32-byte public key = last 32 bytes of the X.509 DER — this is what the frontend needs (§9.4).


---

## 5. Database Schema (Flyway, MySQL 8.4)

**Conventions:** `BIGINT UNSIGNED AUTO_INCREMENT` internal PK; `public_id CHAR(36)` UUID for API exposure; `DATETIME(3)` in **UTC**; money `DECIMAL(10,2)`; `utf8mb4`, InnoDB. Every table has `created_at`; mutable ones also `updated_at`. Migrations are named `V{n}__{desc}.sql` and are **never edited after merge**.

| Migration | Contents |
|-----------|----------|
| V1 | users, user_roles, refresh_tokens, otp_challenges, consents, kyc_verifications |
| V2 | societies, society_members, halls, hall_photos, hall_opening_hours, hall_blackouts, hall_price_rules, hall_staff |
| V3 | bookings, booking_cells, booking_events, idempotency_keys |
| V4 | payments, refunds |
| V5 | entry_credentials, entry_logs, hall_live_status |
| V6 | handover_reports, handover_photos, ratings, trust_scores, disputes, dispute_evidence |
| V7 | decorators, decorator_packages, decorator_blackouts, decorator_enquiries |
| V8 | notification_outbox, notifications, audit_log |
| V9 | waitlist_entries, booking_cohosts (stretch), pricing_suggestions |
| V10 | localities (seeded Pune localities for the locality picker) |
| V11 | push_subscriptions (P3, N-12 only) |

### V1 — Identity & access

```sql
CREATE TABLE users (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  phone VARCHAR(16) NOT NULL,                       -- E.164, e.g. +919876543210
  password_hash VARCHAR(100) NOT NULL,
  status ENUM('PENDING','ACTIVE','SUSPENDED','DELETED') NOT NULL DEFAULT 'PENDING',
  email_verified_at DATETIME(3) NULL,
  phone_verified_at DATETIME(3) NULL,
  preferred_language ENUM('en','hi','mr') NOT NULL DEFAULT 'en',
  profile_photo_path VARCHAR(255) NULL,
  last_login_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_users_public_id (public_id),
  UNIQUE KEY uq_users_email (email),
  UNIQUE KEY uq_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT UNSIGNED NOT NULL,
  role ENUM('RESIDENT','HALL_OWNER','WATCHMAN','DECORATOR','ADMIN') NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE refresh_tokens (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL,                     -- SHA-256 of opaque token
  family_id CHAR(36) NOT NULL,                      -- rotation family (reuse detection)
  expires_at DATETIME(3) NOT NULL,
  revoked_at DATETIME(3) NULL,
  replaced_by BIGINT UNSIGNED NULL,
  user_agent VARCHAR(255) NULL,
  ip VARCHAR(45) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_refresh_hash (token_hash),
  KEY ix_refresh_user (user_id),
  KEY ix_refresh_family (family_id),
  CONSTRAINT fk_refresh_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE otp_challenges (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  purpose ENUM('EMAIL_VERIFY','PHONE_VERIFY','PASSWORD_RESET','GATE_ENTRY','KYC') NOT NULL,
  user_id BIGINT UNSIGNED NULL,
  booking_id BIGINT UNSIGNED NULL,                  -- for GATE_ENTRY
  target VARCHAR(190) NOT NULL,                     -- email or phone (masked in logs)
  code_hash CHAR(64) NOT NULL,                      -- HMAC-SHA256(code, otp_pepper)
  attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
  max_attempts TINYINT UNSIGNED NOT NULL DEFAULT 3,
  expires_at DATETIME(3) NOT NULL,
  verified_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_otp_public (public_id),
  KEY ix_otp_user_purpose (user_id, purpose, created_at),
  KEY ix_otp_booking (booking_id)
) ENGINE=InnoDB;

CREATE TABLE consents (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  purpose ENUM('KYC','LOCATION','MARKETING','TERMS','PRIVACY_POLICY') NOT NULL,
  policy_version VARCHAR(20) NOT NULL,
  granted_at DATETIME(3) NOT NULL,
  revoked_at DATETIME(3) NULL,
  ip VARCHAR(45) NULL,
  KEY ix_consent_user (user_id, purpose),
  CONSTRAINT fk_consent_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE kyc_verifications (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  provider VARCHAR(30) NOT NULL,                    -- MOCK | AADHAAR_OFFLINE | DIGILOCKER | ...
  provider_ref VARCHAR(100) NULL,                   -- opaque reference from provider
  status ENUM('PENDING','VERIFIED','FAILED','EXPIRED','REVOKED') NOT NULL DEFAULT 'PENDING',
  verified_name VARCHAR(255) NULL,                  -- optionally AES-GCM encrypted
  masked_id VARCHAR(20) NULL,                       -- e.g. XXXX-XXXX-1234 (last 4 only, from provider)
  mobile_linked TINYINT(1) NOT NULL DEFAULT 0,      -- provider confirms the ID-linked mobile == users.phone
  consent_id BIGINT UNSIGNED NULL,
  verified_at DATETIME(3) NULL,
  expires_at DATETIME(3) NULL,
  failure_reason VARCHAR(120) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  KEY ix_kyc_user (user_id, status),
  CONSTRAINT fk_kyc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_kyc_consent FOREIGN KEY (consent_id) REFERENCES consents(id)
) ENGINE=InnoDB;
```
> **Never** add a column for a full Aadhaar number, a biometric, or an Aadhaar photo.

### V2 — Societies & halls

```sql
CREATE TABLE societies (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  name VARCHAR(160) NOT NULL,
  address_line VARCHAR(255) NOT NULL,
  locality VARCHAR(100) NOT NULL,
  city VARCHAR(80) NOT NULL,
  pincode CHAR(6) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  manager_user_id BIGINT UNSIGNED NOT NULL,
  registration_doc_path VARCHAR(255) NULL,
  verification_status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
  rejection_reason VARCHAR(255) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_soc_public (public_id),
  KEY ix_soc_manager (manager_user_id),
  CONSTRAINT fk_soc_manager FOREIGN KEY (manager_user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE society_members (
  society_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  flat_label VARCHAR(30) NULL,
  status ENUM('PENDING','APPROVED','REMOVED') NOT NULL DEFAULT 'PENDING',
  approved_at DATETIME(3) NULL,
  PRIMARY KEY (society_id, user_id),
  CONSTRAINT fk_sm_soc FOREIGN KEY (society_id) REFERENCES societies(id) ON DELETE CASCADE,
  CONSTRAINT fk_sm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE halls (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  society_id BIGINT UNSIGNED NOT NULL,
  owner_user_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(160) NOT NULL,
  description TEXT NULL,
  address_line VARCHAR(255) NOT NULL,
  locality VARCHAR(100) NOT NULL,
  city VARCHAR(80) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  capacity_seated SMALLINT UNSIGNED NOT NULL,
  capacity_standing SMALLINT UNSIGNED NOT NULL,
  area_sqft SMALLINT UNSIGNED NULL,
  layout_type ENUM('OPEN_HALL','STAGE_HALL','COURTYARD','TERRACE','ROOM','MULTI_ROOM') NOT NULL DEFAULT 'OPEN_HALL',
  ceiling_height_ft DECIMAL(4,1) NULL,
  indoor TINYINT(1) NOT NULL DEFAULT 1,
  has_ac TINYINT(1) NOT NULL DEFAULT 0,
  has_parking TINYINT(1) NOT NULL DEFAULT 0,
  has_kitchen TINYINT(1) NOT NULL DEFAULT 0,
  has_stage TINYINT(1) NOT NULL DEFAULT 0,
  has_power_backup TINYINT(1) NOT NULL DEFAULT 0,
  has_washroom TINYINT(1) NOT NULL DEFAULT 1,
  power_points SMALLINT UNSIGNED NULL,
  rules_text TEXT NULL,
  base_price_per_hour DECIMAL(10,2) NOT NULL,
  min_slot_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 120,
  max_slot_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 240,
  buffer_after_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 30,      -- turnover/cleaning (exclusive cells)
  quiet_hours_start TIME NULL,                                     -- e.g. 22:00 (hall local time)
  quiet_hours_end TIME NULL,                                       -- e.g. 07:00
  latest_end_time TIME NULL,                                       -- e.g. 22:00
  advance_days_public SMALLINT UNSIGNED NOT NULL DEFAULT 30,
  advance_days_member SMALLINT UNSIGNED NOT NULL DEFAULT 60,
  member_discount_percent DECIMAL(4,1) NOT NULL DEFAULT 0,
  cancellation_policy ENUM('FLEXIBLE','MODERATE','STRICT') NOT NULL DEFAULT 'MODERATE',
  overstay_fee_per_15min DECIMAL(10,2) NOT NULL DEFAULT 0,
  status ENUM('DRAFT','PENDING_APPROVAL','ACTIVE','SUSPENDED','REJECTED') NOT NULL DEFAULT 'DRAFT',
  rejection_reason VARCHAR(255) NULL,
  rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0,
  rating_count INT UNSIGNED NOT NULL DEFAULT 0,
  trust_score DECIMAL(5,2) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_hall_public (public_id),
  KEY ix_hall_status_geo (status, lat, lng),
  KEY ix_hall_owner (owner_user_id),
  KEY ix_hall_society (society_id),
  CONSTRAINT fk_hall_soc FOREIGN KEY (society_id) REFERENCES societies(id),
  CONSTRAINT fk_hall_owner FOREIGN KEY (owner_user_id) REFERENCES users(id),
  CONSTRAINT ck_hall_slot CHECK (min_slot_minutes % 30 = 0 AND max_slot_minutes % 30 = 0 AND min_slot_minutes <= max_slot_minutes),
  CONSTRAINT ck_hall_cap CHECK (capacity_seated > 0 AND capacity_standing >= capacity_seated)
) ENGINE=InnoDB;

CREATE TABLE hall_photos (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  caption VARCHAR(160) NULL,
  sort_order SMALLINT NOT NULL DEFAULT 0,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_hp_hall (hall_id, sort_order),
  CONSTRAINT fk_hp_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hall_opening_hours (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  day_of_week TINYINT UNSIGNED NOT NULL,           -- 1=Mon ... 7=Sun (ISO)
  open_time TIME NOT NULL,                         -- hall-local
  close_time TIME NOT NULL,
  UNIQUE KEY uq_hoh (hall_id, day_of_week, open_time),
  CONSTRAINT fk_hoh_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
  CONSTRAINT ck_hoh CHECK (day_of_week BETWEEN 1 AND 7 AND open_time < close_time)
) ENGINE=InnoDB;

CREATE TABLE hall_blackouts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,                   -- UTC
  end_at DATETIME(3) NOT NULL,
  reason VARCHAR(160) NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  KEY ix_hb_hall (hall_id, start_at, end_at),
  CONSTRAINT fk_hb_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hall_price_rules (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  label VARCHAR(60) NOT NULL,                      -- "Weekend evening"
  days_mask TINYINT UNSIGNED NOT NULL,             -- bit0=Mon ... bit6=Sun
  from_time TIME NOT NULL,
  to_time TIME NOT NULL,
  price_per_hour DECIMAL(10,2) NOT NULL,
  priority SMALLINT NOT NULL DEFAULT 0,            -- higher wins
  active TINYINT(1) NOT NULL DEFAULT 1,
  KEY ix_hpr_hall (hall_id, active, priority),
  CONSTRAINT fk_hpr_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE hall_staff (
  hall_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  staff_role ENUM('WATCHMAN','MANAGER') NOT NULL DEFAULT 'WATCHMAN',
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (hall_id, user_id),
  CONSTRAINT fk_hs_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE,
  CONSTRAINT fk_hs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### V3 — Bookings & the slot engine

```sql
CREATE TABLE bookings (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_ref VARCHAR(20) NOT NULL,                -- SS-2610-7K3QF
  hall_id BIGINT UNSIGNED NOT NULL,
  renter_user_id BIGINT UNSIGNED NOT NULL,
  event_type ENUM('BIRTHDAY','KITTY_PARTY','MEETING','TUITION_BATCH','FESTIVAL','GET_TOGETHER','BABY_SHOWER','WORKSHOP','OTHER') NOT NULL,
  event_title VARCHAR(160) NOT NULL,
  theme_tags JSON NULL,                            -- ["balloon","kids"]
  guest_count SMALLINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,                   -- UTC, aligned to :00/:30
  end_at DATETIME(3) NOT NULL,
  status ENUM('PENDING_PAYMENT','CONFIRMED','CHECKED_IN','CHECKED_OUT','COMPLETED','CANCELLED','EXPIRED','NO_SHOW') NOT NULL,
  lock_expires_at DATETIME(3) NULL,
  price_base DECIMAL(10,2) NOT NULL,
  price_member_discount DECIMAL(10,2) NOT NULL DEFAULT 0,
  price_platform_fee DECIMAL(10,2) NOT NULL DEFAULT 0,
  price_tax DECIMAL(10,2) NOT NULL DEFAULT 0,
  price_total DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'INR',
  is_member_booking TINYINT(1) NOT NULL DEFAULT 0,
  cancellation_policy ENUM('FLEXIBLE','MODERATE','STRICT') NOT NULL,   -- snapshot at booking time
  cancelled_by ENUM('RENTER','OWNER','ADMIN','SYSTEM') NULL,
  cancel_reason VARCHAR(255) NULL,
  cancelled_at DATETIME(3) NULL,
  checked_in_at DATETIME(3) NULL,
  checked_out_at DATETIME(3) NULL,
  arrived_headcount SMALLINT UNSIGNED NULL,
  peak_headcount SMALLINT UNSIGNED NULL,
  overstay_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  dispute_open TINYINT(1) NOT NULL DEFAULT 0,
  rating_window_closes_at DATETIME(3) NULL,
  version INT NOT NULL DEFAULT 0,                  -- optimistic locking
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_booking_public (public_id),
  UNIQUE KEY uq_booking_ref (booking_ref),
  KEY ix_booking_hall_time (hall_id, start_at),
  KEY ix_booking_renter (renter_user_id, start_at),
  KEY ix_booking_status_lock (status, lock_expires_at),
  KEY ix_booking_status_start (status, start_at),
  CONSTRAINT fk_booking_hall FOREIGN KEY (hall_id) REFERENCES halls(id),
  CONSTRAINT fk_booking_renter FOREIGN KEY (renter_user_id) REFERENCES users(id),
  CONSTRAINT ck_booking_time CHECK (end_at > start_at)
) ENGINE=InnoDB;

-- THE double-booking guarantee: one row per hall x 30-minute cell.
CREATE TABLE booking_cells (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  cell_start DATETIME(0) NOT NULL,                 -- UTC, minute in (0,30), second 0
  booking_id BIGINT UNSIGNED NOT NULL,
  cell_type ENUM('BOOKED','BUFFER','SETUP') NOT NULL DEFAULT 'BOOKED',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_cell (hall_id, cell_start),
  KEY ix_cell_booking (booking_id),
  CONSTRAINT fk_cell_hall FOREIGN KEY (hall_id) REFERENCES halls(id),
  CONSTRAINT fk_cell_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
  CONSTRAINT ck_cell_aligned CHECK (MINUTE(cell_start) IN (0,30) AND SECOND(cell_start) = 0)
) ENGINE=InnoDB;

CREATE TABLE booking_events (                       -- append-only audit trail
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  event_type ENUM('CREATED','PAID','QR_ISSUED','CHECKED_IN','CHECKED_OUT','RATED_BY_RENTER','RATED_BY_OWNER',
                  'CANCELLED','EXPIRED','NO_SHOW','REFUNDED','DISPUTE_RAISED','DISPUTE_RESOLVED','COMPLETED',
                  'GUEST_COUNT_CHANGED','DECORATOR_LINKED') NOT NULL,
  from_status VARCHAR(20) NULL,
  to_status VARCHAR(20) NULL,
  actor_user_id BIGINT UNSIGNED NULL,
  actor_role VARCHAR(20) NULL,                     -- RESIDENT | WATCHMAN | HALL_OWNER | ADMIN | SYSTEM
  detail JSON NULL,
  occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_be_booking (booking_id, occurred_at),
  CONSTRAINT fk_be_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE idempotency_keys (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  idem_key VARCHAR(80) NOT NULL,
  endpoint VARCHAR(80) NOT NULL,
  request_hash CHAR(64) NOT NULL,
  response_status SMALLINT NULL,
  response_body JSON NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_idem (user_id, endpoint, idem_key)
) ENGINE=InnoDB;
```

### V4 — Payments

```sql
CREATE TABLE payments (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  provider VARCHAR(20) NOT NULL,                   -- MOCK | RAZORPAY
  provider_order_id VARCHAR(80) NOT NULL,
  provider_payment_id VARCHAR(80) NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'INR',
  status ENUM('CREATED','AUTHORIZED','CAPTURED','FAILED','REFUNDED','PARTIALLY_REFUNDED') NOT NULL DEFAULT 'CREATED',
  signature_verified TINYINT(1) NOT NULL DEFAULT 0,
  failure_reason VARCHAR(160) NULL,
  raw_payload JSON NULL,                           -- sanitised gateway payload
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_pay_public (public_id),
  UNIQUE KEY uq_pay_order (provider, provider_order_id),
  UNIQUE KEY uq_pay_payment (provider, provider_payment_id),
  KEY ix_pay_booking (booking_id),
  CONSTRAINT fk_pay_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB;

CREATE TABLE refunds (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  payment_id BIGINT UNSIGNED NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  reason ENUM('RENTER_CANCEL','OWNER_CANCEL','ADMIN','DISPUTE') NOT NULL,
  provider_refund_id VARCHAR(80) NULL,
  status ENUM('PENDING','PROCESSED','FAILED') NOT NULL DEFAULT 'PENDING',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_ref_booking (booking_id),
  CONSTRAINT fk_ref_pay FOREIGN KEY (payment_id) REFERENCES payments(id)
) ENGINE=InnoDB;
```

### V5 — Entry

```sql
CREATE TABLE entry_credentials (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  jti CHAR(32) NOT NULL,                           -- random 128-bit hex, in QR payload
  booking_id BIGINT UNSIGNED NOT NULL,
  kind ENUM('HOLDER','DECORATOR') NOT NULL DEFAULT 'HOLDER',
  decorator_enquiry_id BIGINT UNSIGNED NULL,
  valid_from DATETIME(3) NOT NULL,
  valid_until DATETIME(3) NOT NULL,
  key_id VARCHAR(20) NOT NULL,
  revoked_at DATETIME(3) NULL,
  revoke_reason VARCHAR(120) NULL,
  issued_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_cred_jti (jti),
  KEY ix_cred_booking (booking_id, kind),
  CONSTRAINT fk_cred_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE entry_logs (                            -- append-only
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  client_event_id CHAR(36) NULL,                   -- offline idempotency (UUID from device)
  booking_id BIGINT UNSIGNED NULL,
  hall_id BIGINT UNSIGNED NOT NULL,
  credential_id BIGINT UNSIGNED NULL,
  watchman_user_id BIGINT UNSIGNED NULL,
  event_type ENUM('SCAN','OTP_SENT','OTP_VERIFIED','CHECK_IN','REENTRY','CHECK_OUT','HEADCOUNT','CAPACITY_ALERT',
                  'WRAP_UP_ALERT','OVERSTAY_ALERT','DECORATOR_IN','DECORATOR_OUT','SYNC_CONFLICT') NOT NULL,
  verdict ENUM('GO','HOLD','STOP') NULL,
  reason_code VARCHAR(40) NULL,
  identity_method ENUM('OTP','MANUAL_OFFLINE','NONE') NULL,
  offline TINYINT(1) NOT NULL DEFAULT 0,
  headcount SMALLINT UNSIGNED NULL,
  device_id VARCHAR(64) NULL,
  meta JSON NULL,
  occurred_at DATETIME(3) NOT NULL,                -- device time if offline, else server time
  recorded_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_el_client (client_event_id),
  KEY ix_el_booking (booking_id, occurred_at),
  KEY ix_el_hall (hall_id, occurred_at),
  CONSTRAINT fk_el_hall FOREIGN KEY (hall_id) REFERENCES halls(id)
) ENGINE=InnoDB;

CREATE TABLE hall_live_status (
  hall_id BIGINT UNSIGNED PRIMARY KEY,
  status ENUM('FREE','OCCUPIED','CLEANING') NOT NULL DEFAULT 'FREE',
  current_booking_id BIGINT UNSIGNED NULL,
  current_headcount SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_hls_hall FOREIGN KEY (hall_id) REFERENCES halls(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### V6 — Handover, ratings, trust, disputes

```sql
CREATE TABLE handover_reports (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  phase ENUM('BEFORE','AFTER') NOT NULL,
  checklist JSON NOT NULL,                         -- {"lightsOff":true,"acOff":true,"furnitureInPlace":true,"floorClean":false,"noDamage":true,"keysReturned":true}
  checklist_score DECIMAL(4,3) NOT NULL,           -- share of OK items, 0..1
  notes VARCHAR(1000) NULL,
  recorded_by BIGINT UNSIGNED NOT NULL,
  recorded_at DATETIME(3) NOT NULL,
  UNIQUE KEY uq_hr_phase (booking_id, phase),
  CONSTRAINT fk_hr_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE handover_photos (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  report_id BIGINT UNSIGNED NOT NULL,
  file_path VARCHAR(255) NOT NULL,
  sha256 CHAR(64) NOT NULL,
  taken_at DATETIME(3) NOT NULL,
  caption VARCHAR(160) NULL,
  CONSTRAINT fk_hph_report FOREIGN KEY (report_id) REFERENCES handover_reports(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE ratings (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  rater_user_id BIGINT UNSIGNED NOT NULL,
  rater_side ENUM('RENTER','HALL_SIDE') NOT NULL,   -- HALL_SIDE = owner or watchman
  subject_type ENUM('HALL','RENTER','DECORATOR') NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,              -- hall.id / user.id / decorator.id
  stars TINYINT UNSIGNED NOT NULL,
  dimensions JSON NULL,                             -- {"cleanliness":5,"accuracy":4,"facilities":4,"helpfulness":5} or {"punctuality":5,"care":4,"conduct":5}
  comment VARCHAR(1000) NULL,
  weight DECIMAL(3,2) NOT NULL DEFAULT 1.00,        -- lowered by collusion check
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_rating (booking_id, rater_side, subject_type),
  KEY ix_rating_subject (subject_type, subject_id, created_at),
  CONSTRAINT fk_rating_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT ck_stars CHECK (stars BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE trust_scores (
  subject_type ENUM('RESIDENT','HALL','DECORATOR') NOT NULL,
  subject_id BIGINT UNSIGNED NOT NULL,
  score DECIMAL(5,2) NOT NULL,
  badge ENUM('NEW','STANDARD','TRUSTED','WATCH') NOT NULL,
  verified_stays INT UNSIGNED NOT NULL DEFAULT 0,
  components JSON NOT NULL,                        -- explainability payload
  computed_at DATETIME(3) NOT NULL,
  PRIMARY KEY (subject_type, subject_id)
) ENGINE=InnoDB;

CREATE TABLE disputes (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  raised_by BIGINT UNSIGNED NOT NULL,
  against_side ENUM('RENTER','HALL_SIDE') NOT NULL,
  category ENUM('DAMAGE','OVERSTAY','NO_ACCESS','MISREPRESENTED_LISTING','CLEANLINESS','OTHER') NOT NULL,
  description VARCHAR(2000) NOT NULL,
  claimed_amount DECIMAL(10,2) NULL,
  status ENUM('OPEN','UNDER_REVIEW','RESOLVED_FOR_RAISER','RESOLVED_AGAINST_RAISER','PARTIAL','WITHDRAWN') NOT NULL DEFAULT 'OPEN',
  resolution_note VARCHAR(2000) NULL,
  resolved_by BIGINT UNSIGNED NULL,
  resolved_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_disp_public (public_id),
  KEY ix_disp_booking (booking_id),
  KEY ix_disp_status (status, created_at),
  CONSTRAINT fk_disp_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
) ENGINE=InnoDB;

CREATE TABLE dispute_evidence (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  dispute_id BIGINT UNSIGNED NOT NULL,
  submitted_by BIGINT UNSIGNED NOT NULL,
  file_path VARCHAR(255) NULL,
  sha256 CHAR(64) NULL,
  note VARCHAR(1000) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  CONSTRAINT fk_de_disp FOREIGN KEY (dispute_id) REFERENCES disputes(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

### V7 — Decorators

```sql
CREATE TABLE decorators (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  business_name VARCHAR(160) NOT NULL,
  description TEXT NULL,
  base_lat DECIMAL(9,6) NOT NULL,
  base_lng DECIMAL(9,6) NOT NULL,
  service_radius_km DECIMAL(5,1) NOT NULL DEFAULT 10,
  portfolio_paths JSON NULL,
  verification_status ENUM('PENDING','APPROVED','REJECTED','SUSPENDED') NOT NULL DEFAULT 'PENDING',
  rating_avg DECIMAL(3,2) NOT NULL DEFAULT 0,
  rating_count INT UNSIGNED NOT NULL DEFAULT 0,
  trust_score DECIMAL(5,2) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_dec_public (public_id),
  UNIQUE KEY uq_dec_user (user_id),
  KEY ix_dec_geo (verification_status, base_lat, base_lng),
  CONSTRAINT fk_dec_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE decorator_packages (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  decorator_id BIGINT UNSIGNED NOT NULL,
  name VARCHAR(120) NOT NULL,
  description TEXT NULL,
  event_types JSON NOT NULL,                       -- ["BIRTHDAY","BABY_SHOWER"] or ["ANY"]
  theme_tags JSON NOT NULL,                        -- ["balloon","floral","kids","traditional"]
  layout_types JSON NOT NULL,                      -- ["OPEN_HALL","STAGE_HALL"] or ["ANY"]
  min_capacity SMALLINT UNSIGNED NOT NULL,
  max_capacity SMALLINT UNSIGNED NOT NULL,
  base_price DECIMAL(10,2) NOT NULL,
  price_per_guest DECIMAL(8,2) NOT NULL DEFAULT 0,
  setup_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 60,
  teardown_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 30,
  active TINYINT(1) NOT NULL DEFAULT 1,
  KEY ix_dp_dec (decorator_id, active),
  CONSTRAINT fk_dp_dec FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE,
  CONSTRAINT ck_dp_cap CHECK (min_capacity <= max_capacity)
) ENGINE=InnoDB;

CREATE TABLE decorator_blackouts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  decorator_id BIGINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,
  end_at DATETIME(3) NOT NULL,
  reason VARCHAR(120) NULL,
  KEY ix_db_dec (decorator_id, start_at, end_at),
  CONSTRAINT fk_db_dec FOREIGN KEY (decorator_id) REFERENCES decorators(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE decorator_enquiries (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  public_id CHAR(36) NOT NULL,
  booking_id BIGINT UNSIGNED NOT NULL,
  decorator_id BIGINT UNSIGNED NOT NULL,
  package_id BIGINT UNSIGNED NULL,
  renter_user_id BIGINT UNSIGNED NOT NULL,
  message VARCHAR(1000) NULL,
  match_score DECIMAL(5,2) NULL,
  match_explanation JSON NULL,
  status ENUM('SENT','ACCEPTED','DECLINED','EXPIRED','CONFIRMED_BY_RENTER','CANCELLED') NOT NULL DEFAULT 'SENT',
  quoted_price DECIMAL(10,2) NULL,
  setup_window_start DATETIME(3) NULL,
  setup_window_end DATETIME(3) NULL,
  teardown_window_end DATETIME(3) NULL,
  responded_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  UNIQUE KEY uq_enq_public (public_id),
  KEY ix_enq_dec (decorator_id, status),
  KEY ix_enq_booking (booking_id),
  CONSTRAINT fk_enq_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
  CONSTRAINT fk_enq_dec FOREIGN KEY (decorator_id) REFERENCES decorators(id)
) ENGINE=InnoDB;
```

### V8 — Notifications & audit

```sql
CREATE TABLE notification_outbox (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NULL,
  channel ENUM('EMAIL','SMS','IN_APP','PUSH') NOT NULL,
  template_code VARCHAR(60) NOT NULL,
  language ENUM('en','hi','mr') NOT NULL DEFAULT 'en',
  destination VARCHAR(190) NULL,                  -- email/phone
  payload JSON NOT NULL,
  status ENUM('PENDING','SENT','FAILED','DEAD') NOT NULL DEFAULT 'PENDING',
  attempts TINYINT UNSIGNED NOT NULL DEFAULT 0,
  next_attempt_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  last_error VARCHAR(255) NULL,
  dedupe_key VARCHAR(120) NULL,                   -- prevents duplicate reminders
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  sent_at DATETIME(3) NULL,
  UNIQUE KEY uq_outbox_dedupe (dedupe_key, channel),
  KEY ix_outbox_due (status, next_attempt_at)
) ENGINE=InnoDB;

CREATE TABLE notifications (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT UNSIGNED NOT NULL,
  type VARCHAR(60) NOT NULL,
  title VARCHAR(160) NOT NULL,
  body VARCHAR(500) NOT NULL,
  link VARCHAR(255) NULL,
  read_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_notif_user (user_id, read_at, created_at),
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE audit_log (                           -- admin & sensitive actions, append-only
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  actor_user_id BIGINT UNSIGNED NULL,
  action VARCHAR(60) NOT NULL,                     -- HALL_APPROVED, USER_SUSPENDED, DISPUTE_RESOLVED, KYC_REVOKED ...
  entity_type VARCHAR(40) NOT NULL,
  entity_id VARCHAR(40) NOT NULL,
  detail JSON NULL,
  ip VARCHAR(45) NULL,
  occurred_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_audit_entity (entity_type, entity_id),
  KEY ix_audit_actor (actor_user_id, occurred_at)
) ENGINE=InnoDB;
```

### V9 — Added-novelty tables

```sql
CREATE TABLE waitlist_entries (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  start_at DATETIME(3) NOT NULL,
  end_at DATETIME(3) NOT NULL,
  guest_count SMALLINT UNSIGNED NOT NULL,
  event_type VARCHAR(30) NOT NULL,
  status ENUM('WAITING','OFFERED','CONVERTED','EXPIRED','CANCELLED') NOT NULL DEFAULT 'WAITING',
  offered_booking_id BIGINT UNSIGNED NULL,
  offer_expires_at DATETIME(3) NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_wl_hall_time (hall_id, status, start_at),
  KEY ix_wl_user (user_id, status)
) ENGINE=InnoDB;

CREATE TABLE pricing_suggestions (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  hall_id BIGINT UNSIGNED NOT NULL,
  day_of_week TINYINT UNSIGNED NOT NULL,
  from_time TIME NOT NULL,
  to_time TIME NOT NULL,
  suggestion ENUM('DISCOUNT','SURGE') NOT NULL,
  percent DECIMAL(4,1) NOT NULL,
  rationale JSON NOT NULL,                         -- occupancy stats behind it
  status ENUM('NEW','APPLIED','DISMISSED') NOT NULL DEFAULT 'NEW',
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  KEY ix_ps_hall (hall_id, status)
) ENGINE=InnoDB;

-- P3 stretch (N-10): Split-the-Cost
CREATE TABLE booking_cohosts (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  booking_id BIGINT UNSIGNED NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  phone VARCHAR(16) NULL,
  share_amount DECIMAL(10,2) NOT NULL,
  pay_token CHAR(32) NOT NULL,
  status ENUM('INVITED','PAID','EXPIRED') NOT NULL DEFAULT 'INVITED',
  paid_at DATETIME(3) NULL,
  UNIQUE KEY uq_cohost_token (pay_token),
  CONSTRAINT fk_cohost_booking FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
) ENGINE=InnoDB;
```

---

## 6. Core Domain Rules

### 6.1 Time handling
- All instants stored/processed in **UTC**. Hall-local rules (opening hours, quiet hours, price rules) are evaluated in `app.timezone` (`Asia/Kolkata`).
- Cell size = 30 min. `SlotRequest` rejects any start not on :00/:30 and any duration not a multiple of 30.
- `Clock` bean injected everywhere; tests use `Clock.fixed`.

### 6.2 Booking validation (`BookingRulesValidator`)
A booking request `(hallId, startAt, durationMinutes, eventType, guestCount)` is valid only if **all** hold; each failure has a stable error code:

| # | Rule | Error code |
|---|------|-----------|
| 1 | Hall `ACTIVE` and society `APPROVED` | `HALL_NOT_AVAILABLE` |
| 2 | Renter status `ACTIVE`, KYC `VERIFIED` and not expired | `KYC_REQUIRED` |
| 3 | `startAt` aligned to 30 min; duration multiple of 30 within `[min_slot, max_slot]` | `INVALID_SLOT` |
| 4 | `startAt ≥ now + min-lead (120 min)` | `TOO_SOON` |
| 5 | Date within advance window: `advance_days_member` for approved members, else `advance_days_public` | `OUTSIDE_BOOKING_WINDOW` |
| 6 | Whole span inside opening hours for every covered day (split spans across midnight are not allowed in v1) | `OUTSIDE_OPEN_HOURS` |
| 7 | No overlap with a hall blackout | `HALL_BLOCKED` |
| 8 | If `latest_end_time` set: `endAt(local) ≤ latest_end_time` | `AFTER_LATEST_END` |
| 9 | If quiet hours set: span must not intersect `[quiet_start, quiet_end)` (local, may wrap midnight) | `QUIET_HOURS` |
| 10 | `guestCount ≤ capacity_standing` (soft warn above `capacity_seated`) | `OVER_CAPACITY` |
| 11 | Renter has no other `PENDING_PAYMENT` lock for the same hall (prevents lock hoarding); max 2 active locks per user overall | `TOO_MANY_LOCKS` |
| 12 | Renter is not suspended and trust badge ≠ blocked (WATCH users can book but owner is notified — no auto-block) | — |

### 6.3 Slot engine (cells + lock)

**Algorithm — `SlotService.reserve(hall, slot, booking)` inside the booking transaction:**

```java
// 1. Free any expired locks that overlap the wanted window (lazy cleanup)
lockExpiryService.releaseExpiredOverlapping(hallId, windowStart, windowEnd + bufferAfter);

// 2. Build cells: N BOOKED cells for [start, end) plus bufferAfter/30 BUFFER cells
List<Instant> booked  = cellsBetween(start, end);
List<Instant> buffer  = cellsBetween(end, end.plusMinutes(hall.getBufferAfterMinutes()));

// 3. Insert in one JDBC batch. UNIQUE(hall_id, cell_start) is the arbiter.
try {
    jdbc.batchUpdate(
      "INSERT INTO booking_cells (hall_id, cell_start, booking_id, cell_type) VALUES (?,?,?,?)", ...);
} catch (DuplicateKeyException e) {
    throw new SlotUnavailableException();   // whole transaction rolls back (booking row too)
}
```

**Facts to preserve:**
- The unique key makes the outcome correct under any concurrency; **do not add app-level locks or `synchronized`**.
- A `SlotUnavailableException` propagating out of the `@Transactional` method rolls back the booking insert. The `@ControllerAdvice` then calls `AlternativesService` **in a new read-only transaction** to attach suggestions to the 409 response (§19, N-06).
- **Buffer rule:** only a *trailing* `bufferAfterMinutes` (turnover) is exclusive. A following booking may start immediately after the buffer. A *leading* setup window is reserved only when a decorator is confirmed (`cell_type = SETUP`, §13.6).
- **Lock expiry:** `PENDING_PAYMENT` bookings with `lock_expires_at < now` are set `EXPIRED`, their cells deleted, and the waitlist is notified. Done lazily (step 1) and by `LockExpiryJob` every 30 s.
- **Payment received after expiry** (race): if the cells were released and are now taken, mark payment `CAPTURED` but booking stays `EXPIRED` → trigger automatic full refund and notify the user. If the cells are still free, re-acquire them and confirm.

### 6.4 Availability read model
`GET /halls/{id}/availability?from=YYYY-MM-DD&to=YYYY-MM-DD` (max 14 days). For each local date:
1. Start from opening-hour windows.
2. Subtract blackouts.
3. Subtract quiet hours and `latest_end_time` (mark `CLOSED`).
4. Mark cells present in `booking_cells`: `BOOKED` (status CONFIRMED/CHECKED_IN/…), `LOCKED` (booking `PENDING_PAYMENT` and `lock_expires_at > now`), `BUFFER`, `SETUP`.
5. Mark past cells and cells earlier than `now + min-lead` as `PAST`.
Response shape: `days[]{date, cells[]{start, state}}` where `state ∈ FREE|LOCKED|BOOKED|BUFFER|CLOSED|PAST`. Also return `minSlotMinutes`, `maxSlotMinutes`, `priceRulesSummary`.

### 6.5 Pricing (`PriceCalculator`)
```
for each BOOKED cell c:
    rate = highest-priority active price rule matching (local day-of-week mask, local time in [from,to)) else hall.base_price_per_hour
    cell_price = rate / 2
base       = Σ cell_price
discount   = isApprovedMember ? base × member_discount_percent / 100 : 0
subtotal   = base − discount
fee        = subtotal × PLATFORM_FEE_PERCENT / 100
tax        = (subtotal + fee) × TAX_PERCENT / 100         // configurable; not tax advice
total      = subtotal + fee + tax                        // each item rounded HALF_UP to 2 dp
```
The quote is **recomputed server-side at booking creation and at payment-order creation**; the total on the order must equal the booking total.

### 6.6 Cancellation & refunds (`CancellationPolicyService`)

| Policy | Refund of (subtotal + tax) |
|--------|----------------------------|
| **FLEXIBLE** | ≥ 24 h before start: 100% · 6–24 h: 50% · < 6 h: 0% |
| **MODERATE** | ≥ 72 h: 100% · 24–72 h: 50% · < 24 h: 0% |
| **STRICT** | ≥ 7 days: 50% · otherwise 0% |

- The **platform fee** is refunded only when the *owner or system* cancels or when a payment race refund applies.
- **Owner-initiated cancellation:** 100% refund incl. fee, recorded against the hall's reliability (§12) and the renter is offered alternatives.
- `PENDING_PAYMENT` cancel/expire: no money moved; cells released.
- Refund is created as `refunds` row → gateway call → status `PROCESSED`; booking event `REFUNDED`.
- A booking with an **open dispute** cannot be cancelled by the disputing party.

### 6.7 Booking state machine (`BookingStateMachine`)

Allowed transitions (anything else throws `ILLEGAL_STATE_TRANSITION`):

| From | To | Trigger | Actor |
|------|----|---------|-------|
| — | PENDING_PAYMENT | create | RENTER |
| PENDING_PAYMENT | CONFIRMED | payment verified | SYSTEM |
| PENDING_PAYMENT | EXPIRED | lock timeout | SYSTEM |
| PENDING_PAYMENT / CONFIRMED | CANCELLED | cancel | RENTER / OWNER / ADMIN |
| CONFIRMED | CHECKED_IN | OTP verified (or offline sync) | WATCHMAN |
| CONFIRMED | NO_SHOW | `end_at + grace` passed without check-in | SYSTEM |
| CHECKED_IN | CHECKED_OUT | check-out | WATCHMAN (or OWNER) |
| CHECKED_OUT | COMPLETED | both sides rated OR rating window closed | SYSTEM |

Every transition: (a) validates the guard, (b) updates `bookings.status` with optimistic locking (`version`), (c) inserts `booking_events` with actor and detail, (d) enqueues notifications — **all in one transaction**.

### 6.8 Resident-Priority Windows (N-05, applies in P0 validation)
- `society_members.status = APPROVED` → member uses `advance_days_member` (default 60) instead of `advance_days_public` (30) and receives `member_discount_percent`.
- Membership approval is done by the hall owner (society manager) in the Owner UI.


---

## 7. Security Implementation

### 7.1 Authentication
- **Register** → `users.status=PENDING`, roles = `RESIDENT` (default). Hall Owner / Decorator registrations select their role; Owner needs society approval, Decorator needs admin verification. **Watchmen are never self-registered** — created by an owner (`POST /owner/halls/{id}/staff`) with a temporary password + forced change on first login.
- **Verify** email (link/OTP) and phone (OTP) → `ACTIVE`.
- **Login** (`email|phone` + password) → response body `{accessToken, expiresIn, user}`; sets `refresh_token` cookie: `HttpOnly; Secure; SameSite=Strict; Path=/api/v1/auth; Max-Age=7d`.
- **Access JWT (HS256, 15 min)** claims: `sub` = user public id, `roles` = array, `ver` = token version, `iat`, `exp`, `jti`. No PII.
- **Refresh rotation:** each `/auth/refresh` consumes the presented token, issues a new one in the same `family_id`, links `replaced_by`. **Reuse of a revoked token → revoke the whole family** and force re-login.
- **Password policy:** ≥ 10 chars, not in a small deny-list, BCrypt strength 12 (or Argon2id). Lockout: 5 failures → 15 min cooldown (per account + IP).
- **Forgot password:** OTP to email/phone (`PASSWORD_RESET`), single use.

### 7.2 Authorisation matrix

| Capability | RESIDENT | HALL_OWNER | WATCHMAN | DECORATOR | ADMIN |
|------------|:--:|:--:|:--:|:--:|:--:|
| Search / view halls, decorators | ✅ | ✅ | ✅ | ✅ | ✅ |
| KYC, create booking, pay, cancel own | ✅ | ✅* | — | — | — |
| View own bookings / QR | ✅ | ✅ | — | — | ✅ |
| Rate a completed stay (own) | ✅ | — | — | — | — |
| Manage own halls, hours, pricing, blackouts | — | ✅ | — | — | ✅ |
| View bookings/analytics of **own** halls | — | ✅ | limited (today) | — | ✅ |
| Create/deactivate watchmen for **own** halls | — | ✅ | — | — | ✅ |
| Approve society members | — | ✅ | — | — | ✅ |
| Scan / check-in / check-out for **assigned** halls | — | ✅ (as manager) | ✅ | — | — |
| Rate renter after checkout | — | ✅ | ✅ | — | — |
| Manage decorator profile / packages / enquiries | — | — | — | ✅ | ✅ |
| Approve halls/societies/decorators, manage users, resolve disputes | — | — | — | — | ✅ |
\* an owner can also book as a resident (roles are additive) but **not** their own hall.

**Ownership checks** (in services, not only annotations): `hallAccess.requireOwner(hallId, userId)`, `hallAccess.requireStaff(hallId, userId)`, `bookingAccess.requireHolder(bookingId, userId)`. A failed ownership check returns **404** (not 403) for resource-hiding on public ids of other users' bookings; role failures return 403.

### 7.3 Rate limiting (Bucket4j, in-memory per node)
`POST /auth/login` 10/min/IP · OTP send 6/hour/target · OTP verify 10/hour/challenge · `/entry/scan` 60/min/user · enquiries 10/hour/user · booking create 20/hour/user. Exceeding → `429 RATE_LIMITED` with `Retry-After`.

### 7.4 HTTP hardening
Security headers (CSP `default-src 'self'` with allow-list for Razorpay Checkout, OSM tiles, fonts; `X-Content-Type-Options: nosniff`; `Referrer-Policy: strict-origin-when-cross-origin`; `X-Frame-Options: DENY`; HSTS). CORS allow-list from env. Request size limits. Global exception handler hides stack traces; includes `traceId`.

### 7.5 Sensitive data handling
- Logging: MDC `traceId`, `userPublicId`; **mask** phone/email; never log tokens, OTPs, QR tokens, passwords, KYC payloads. A `LogSanitizer` unit test asserts masking.
- `verified_name` optional AES-256-GCM at rest (`PII_ENCRYPTION_KEY_B64`); key rotation documented.
- Upload pipeline: size limit, magic-byte MIME sniff (JPEG/PNG/WebP/PDF), re-encode images (strip EXIF GPS), random filename, store under `FILE_STORAGE_PATH`, serve through an authorised endpoint (never a public static dir), record SHA-256.

---

## 8. KYC / Identity Module

### 8.1 Principles
1. **SmartSpace never receives, stores, or logs a raw Aadhaar number.** Verification happens on a provider-hosted flow (real) or a **mock hosted page** (dev/demo) and returns only a result.
2. Store only: `provider`, `provider_ref`, `status`, `verified_name`, `masked_id` (last 4), `mobile_linked`, timestamps, `consent_id`.
3. Consent first; revocable; documented purpose.
4. Real Aadhaar/e-KYC use is regulated — see `PRD.md` R1. Everything here is behind an interface so a lawful provider can be plugged in later.

### 8.2 Port & adapters
```java
public interface IdentityProvider {
    String code();                                              // "MOCK", "DIGILOCKER", ...
    KycSession start(KycStartRequest req);                      // returns sessionId + redirectUrl
    KycResult fetchResult(String sessionId);                    // server-to-server result
}
record KycResult(boolean verified, String providerRef, String verifiedName,
                 String maskedId, boolean mobileLinked, String failureReason) {}
```
`MockIdentityProvider`: `start` returns `/kyc/mock?session=<id>`. The mock page (frontend, dev profile only) lets the tester pick a scenario — **Verified**, **Verified (mobile not linked)**, **Name mismatch**, **Failed** — and type a display name. It never asks for an ID number. `fetchResult` returns the chosen outcome with `maskedId = "XXXX-XXXX-" + random4`.

### 8.3 Flow
```
POST /kyc/consent        {policyVersion}              -> consent recorded (purpose=KYC)
POST /kyc/start          {}                           -> {sessionId, redirectUrl}
(user completes provider/mock page)
POST /kyc/complete       {sessionId}                  -> server calls fetchResult -> kyc_verifications VERIFIED|FAILED
GET  /kyc/status                                    -> {status, verifiedName, maskedId, assurance, expiresAt}
POST /kyc/revoke                                      -> user withdraws; status REVOKED; outstanding credentials revoked
```
- **Assurance level** returned to the watchman: `HIGH` if `mobile_linked` and `users.phone_verified_at` set; else `STANDARD`.
- Booking requires `VERIFIED` and `expires_at > now` (default 12 months).
- `KycExpiryJob` warns users 14 days before expiry.

### 8.4 What the watchman sees (from KYC)
Verified name (first name + last initial by default; full name when the owner enables "show full name" for the hall), assurance badge, guest count, booking window. **Never** the ID number, not even masked.

---

## 9. QR Token Module

### 9.1 Token format
```
SS1.<payloadB64Url>.<signatureB64Url>
payload (compact JSON, UTF-8):
  {"v":1,"j":"<jti 32 hex>","k":"H|D","f":<nbfEpochSec>,"x":<expEpochSec>,"i":"<keyId>"}
signature = Ed25519( privateKey, ASCII("SS1." + payloadB64Url) )
```
- Sign the **exact string** `"SS1." + payloadB64Url` — no JSON re-serialisation on verify.
- No PII, no booking id. `j` (128-bit random) maps to `entry_credentials.jti` server-side (and to the offline manifest on the device).
- Typical length ≈ 200 chars → renders as a comfortable QR (ECC level M).
- **Windows:** HOLDER `nbf = start − 30 min`, `exp = end + bufferAfter + 15 min`. DECORATOR windows come from the enquiry (§13.6).
- Optional AES-GCM envelope (`qr.encrypt=true`) exists for online-only deployments; default is *signed, opaque, PII-free* (offline verification requires no secret on the device).

### 9.2 Issuance
`QrTokenService.issueHolderCredential(booking)` — called inside the payment-confirmation transaction: create `entry_credentials` row (`kind=HOLDER`), build/sign token, add `QR_ISSUED` event. Token is **re-derivable** from the row (store nothing but `jti`, window, `key_id`; re-sign on demand with the same fields — signing is deterministic for Ed25519).

### 9.3 Verification (server) — ordered checks
1. Format `SS1.` + 3 parts, sizes sane → else `QR_MALFORMED`
2. `i` (key id) known → else `QR_UNKNOWN_KEY`
3. Ed25519 signature valid → else `QR_INVALID_SIGNATURE`
4. Credential by `jti` exists → else `QR_UNKNOWN`
5. Not revoked → else `QR_REVOKED`
6. Booking status ∈ {`CONFIRMED`, `CHECKED_IN`} (HOLDER) → else `BOOKING_CANCELLED` / `BOOKING_NOT_ACTIVE`
7. Watchman is active staff for the booking's hall → else `WRONG_HALL`
8. `now < valid_from` → `QR_NOT_YET_VALID` (details: opens at HH:MM); `now > valid_until` → `QR_EXPIRED`
9. Kind rules (HOLDER → identity step; DECORATOR → §13.6)

### 9.4 Public keys endpoint (for offline verify)
`GET /api/v1/entry/public-keys` → `{"keys":[{"kid":"k1","alg":"Ed25519","x":"<base64url raw 32-byte public key>"}]}` (authenticated: WATCHMAN, HALL_OWNER). Extract the raw key server-side: last 32 bytes of `publicKey.getEncoded()`.
Frontend verify with `@noble/curves/ed25519`: `ed25519.verify(sigBytes, new TextEncoder().encode("SS1."+payloadB64Url), pubKeyBytes)`.

### 9.5 Revocation
Credentials are revoked on booking cancel/expiry, KYC revoke, owner "block entry", or admin action. Revocations appear in the next manifest (`revokedJtis`) and are enforced immediately online.

### 9.6 Reason codes → copy

| Code | Verdict | Watchman copy (EN) |
|------|---------|--------------------|
| `OK` | HOLD→GO | "Ask for the code" → "Let them in" |
| `QR_MALFORMED`, `QR_UNKNOWN_KEY`, `QR_INVALID_SIGNATURE`, `QR_UNKNOWN` | STOP | "This is not a valid pass" |
| `QR_REVOKED`, `BOOKING_CANCELLED`, `BOOKING_NOT_ACTIVE` | STOP | "Booking cancelled" |
| `WRONG_HALL` | STOP | "This booking is for a different hall" |
| `QR_NOT_YET_VALID` | STOP | "Too early — opens at {time}" |
| `QR_EXPIRED` | STOP | "Pass has expired" |
| `ALREADY_CHECKED_IN` | HOLD | "Already inside — re-entry?" |
| `OTP_FAILED` / `OTP_LOCKED` | STOP | "Wrong code" / "Too many tries — call owner" |
| `CAPACITY_EXCEEDED` | HOLD | "Hall is full — ask owner" |
| `KYC_NOT_VERIFIED` | STOP | "Identity not verified — call owner" |

---

## 10. Entry, Watchman & Offline Mode

### 10.1 Scan → verdict (online)

`POST /entry/scan {token, deviceId}`
```
result = qr.verify(token, watchman)                      // §9.3
if (result.fail)                     -> log SCAN(STOP, reason); return STOP
booking = result.booking
if booking.status == CHECKED_IN      -> return HOLD(reason=ALREADY_CHECKED_IN, mode=REENTRY)
if kind == DECORATOR                 -> §13.6
kyc = kycService.status(booking.renter)
if kyc != VERIFIED                   -> STOP(KYC_NOT_VERIFIED)
challenge = otpService.createGateChallenge(booking, user.phone)  // rate limited; 3 attempts; 180 s
log SCAN(HOLD), OTP_SENT
return HOLD { bookingRef, hallName, displayName, assurance, guestsExpected, window,
              maskedPhone:"XXXXXX3210", challengeId, expiresAt }
```

`POST /entry/verify-otp {challengeId, otp, arrivedCount?}`
```
challenge = otpService.verify(challengeId, otp)      // constant-time; increments attempts; locks at max
if (!ok) log(STOP, OTP_FAILED|OTP_LOCKED); return STOP
capacity guard (§19 N-02): if arrivedCount > hall.capacity_standing -> HOLD(CAPACITY_EXCEEDED)
bookingStateMachine.checkIn(booking, watchman, arrivedCount, identityMethod=OTP)
hall_live_status = OCCUPIED (current_booking_id, headcount = arrivedCount)
log OTP_VERIFIED, CHECK_IN; notify owner + renter ("Checked in at 5:04 pm")
return GO { displayName, guestsExpected, endsAt }
```

**Re-entry** (`ALREADY_CHECKED_IN`): watchman confirms "same person?" and taps **Allow**; logs `REENTRY` (no OTP, since identity was verified for this booking). Optional owner setting "OTP on every re-entry".

### 10.2 Check-out
`POST /entry/checkout {bookingId, checklist{…}, notes, finalHeadcount, photos[]}` (multipart or pre-uploaded photo ids)
- Allowed only for `CHECKED_IN` bookings at the watchman's hall (or the owner).
- Computes `overstay_minutes = max(0, now − (end_at + bufferAfter?))` — overstay is measured against **`end_at`** (not the buffer).
- Writes `handover_reports(AFTER)`, `entry_logs(CHECK_OUT)`, `checked_out_at`, `booking_events(CHECKED_OUT)`, sets hall status `CLEANING` until buffer ends then `FREE`.
- Opens the rating window (7 days): `rating_window_closes_at = now + 7d`.

### 10.3 Watchman app behaviours
- Home: today's bookings for assigned hall(s), online/offline pill, **SCAN QR** (primary), manual code fallback (paste token / type booking ref + OTP).
- Continuous **wake lock** while scanning; torch toggle; vibrate + beep on verdict.
- After verdict, auto-return to Home after 8 s on GO/STOP.
- Language switch stays visible; default language = watchman profile language.

### 10.4 Offline mode (N-01) — build spec

**Manifest** `GET /api/v1/entry/manifest?hallIds=…&hours=24` (WATCHMAN/OWNER): returns
```json
{
  "generatedAt": "2026-10-24T08:00:00Z",
  "expiresAt":   "2026-10-25T08:00:00Z",
  "keys": [{"kid":"k1","alg":"Ed25519","x":"<b64url 32B>"}],
  "halls": [{"hallId":"…","name":"Green Meadows Hall","capacityStanding":60}],
  "credentials": [{
    "jti":"…","kind":"HOLDER","bookingId":"…","bookingRef":"SS-2610-7K3QF","hallId":"…",
    "validFrom":"…","validUntil":"…","displayName":"Priya S.","guestsExpected":25,
    "assurance":"HIGH","status":"CONFIRMED"
  }],
  "revokedJtis": ["…"]
}
```
- PWA fetches and stores it in IndexedDB on login, every 15 min while online, and on app resume. Contains **no phone numbers, no ID data**.
- **Offline scan:** verify signature with cached key → jti in manifest → not in `revokedJtis` → device time within window → verdict.
  - Offline identity step = **"Compare with ID"**: screen shows verified display name + assurance; watchman taps **ID matches** (→ GO) or **Doesn't match** (→ STOP). Recorded `identity_method = MANUAL_OFFLINE`, `offline = 1`.
- **Queue:** each action is stored in IndexedDB `outbox` with `clientEventId` (UUID), `type`, `jti`, `occurredAt` (device time), `identityMethod`, `headcount`, `notes`; background sync via `online` event + Workbox Background Sync where supported.
- **Sync** `POST /entry/sync {deviceId, events[]}` → per-event `{clientEventId, status: ACCEPTED|DUPLICATE|REJECTED, reason}`:
  - Idempotent on `client_event_id`.
  - Re-validate: credential exists, not revoked at the *time of the event*, event time within window ± 5 min of clock skew.
  - Accepted CHECK_IN → state machine transition if still `CONFIRMED`; if booking already cancelled/checked-in elsewhere → log `SYNC_CONFLICT` and alert the owner (never silently drop).
  - Offline check-ins send the owner a "verified offline, OTP not performed" notice.
- **UI:** amber "Offline — passes are checked on this phone" banner; queue length pill; "Synced ✓" toast.
- **Limits (be honest in the UI/docs):** offline mode has a **lower identity assurance** than online OTP; manifest can be stale by up to 15 min (a very recent cancellation may not be reflected).


---

## 11. Check-out & Handover Evidence Pack (NV-6, N-07)

### 11.1 Checklist (JSON keys → UI rows)
`lightsOff`, `acFansOff`, `furnitureInPlace`, `floorClean`, `noDamage`, `keysReturned` (booleans). `checklist_score = (# true) / 6`. Free-text `notes` (≤ 1000 chars). Up to **4 photos** per report (camera capture preferred).

### 11.2 Phases
- **BEFORE** (optional, at check-in): watchman records pre-existing condition — protects the renter from false damage claims. Default UI: one-tap "Hall looks fine" or add notes/photos.
- **AFTER** (required at check-out).

### 11.3 Photos
Stored per §7.5 (EXIF stripped, SHA-256 recorded, `taken_at` = server receipt time; device time kept in `meta`). Photos are visible only to renter, hall owner/staff, admin.

### 11.4 Evidence PDF (`GET /bookings/{id}/evidence.pdf`)
Built with OpenPDF from an HTML/Java template. Sections: booking summary; **scheduled vs actual** (start, end, check-in, check-out, overstay minutes); headcount (declared / arrived / peak / capacity); lifecycle timeline from `booking_events`; entry log summary (offline flags, decorator visits); BEFORE vs AFTER checklist table with diffs highlighted; photo thumbnails with hash + time; dispute summary (if any); footer with generation time and a **SHA-256 fingerprint** of the serialised events (integrity aid, *not* legal proof). Access: renter, owner, staff of hall, admin.

---

## 12. Ratings & Trust Score (NV-3)

### 12.1 Eligibility ("verified-only" rule)
A rating can be created only if **all** hold:
1. `booking.status ∈ {CHECKED_OUT, COMPLETED}` and both `CHECK_IN` and `CHECK_OUT` rows exist in `entry_logs`.
2. On-site duration ≥ `min(30 min, 25% of the booked slot)`.
3. Within the rating window (7 days after check-out).
4. Rater is the booking's renter (side `RENTER`) or the hall owner/staff of that hall (side `HALL_SIDE`).
5. At most one rating per `(booking, side, subject_type)`; ratings are immutable.
6. Decorator rating only by the renter and only if that decorator's pass has a `DECORATOR_IN` log for the booking.

**Dimensions (1–5 each, `stars` = rounded average):**
- Renter → Hall: `cleanliness`, `accuracy` (listing matches reality), `facilities`, `helpfulness`.
- Hall side → Renter: `punctuality`, `care`, `conduct`.
- Renter → Decorator: `quality`, `punctuality`, `professionalism`.

### 12.2 Anti-gaming
- **Collusion down-weight:** for a `(renter, hall)` pair, ratings from the 4th booking within 90 days onward get `weight = 0.5`.
- **Bayesian smoothing** stops one or two ratings from dominating.
- **Recency decay:** `decay = max(0.1, 0.5^(ageDays/180))`.
- Suspected collusion is surfaced to Admin (never auto-punished).

### 12.3 Resident trust score (0–100)
For each completed stay *i* (last 365 days):
```
q_i = (stars_from_hall_side − 1) / 4                      // 0..1, may be missing
p_i = clamp(1 − overstayMinutes/60, 0, 1)                 // punctuality from logs
c_i = checklistScoreAfter                                  // 0..1
s_i = rated ? 0.5·q_i + 0.25·p_i + 0.25·c_i : 0.5·p_i + 0.5·c_i
w_i = decay_i × collusion_i × (rated ? 1.0 : 0.5)

penalty = min(0.40, 0.05·L + 0.15·N + 0.20·D)
   L = late cancellations (inside the zero-refund window)
   N = no-shows
   D = upheld disputes against the renter

T_resident = 100 × (1 − penalty) × ( C·m + Σ w_i·s_i ) / ( C + Σ w_i )
   prior mean m = 0.60, prior weight C = 3   →  a brand-new user starts at 60
```
**Badge:** `NEW` if verified stays < 3 · `TRUSTED` if stays ≥ 5, `T ≥ 80`, `D = 0` · `WATCH` if stays ≥ 3 and `T < 40` · else `STANDARD`.

*Worked example:* 4 stays with `s = 0.90, 0.80, 1.00, 0.85`, all recent and rated (`Σw = 4`), one late cancellation (`L = 1`):
`(3·0.60 + 3.55) / (3 + 4) = 0.7643` → ×`(1 − 0.05)` → **72.6**.

### 12.4 Hall trust score
```
r_i = (weightedAvg(cleanliness, accuracy, facilities, helpfulness) − 1) / 4
reliability_penalty = min(0.30, 0.6 × ownerCancellationRate365)
T_hall = 100 × (1 − reliability_penalty) × ( C·m + Σ w_i·r_i ) / ( C + Σ w_i )
   m = 0.70, C = 5
```
Displayed alongside the plain star average and review count. *Example:* 8 stays, `Σ w·r = 6.8`, `Σ w = 8`, 1 owner cancellation in 9 bookings → `(3.5 + 6.8)/13 = 0.792` × `(1 − 0.0667)` → **73.9**.

### 12.5 Decorator trust score
Same form as hall (`m = 0.70`, `C = 5`) using their rating dimensions, with `reliability_penalty` from declined-after-accept, late/no-show arrivals (from `DECORATOR_IN` vs window).

### 12.6 Recompute triggers & explainability
Recompute the affected subject on: rating created, check-out, cancellation, no-show, dispute resolved, and nightly. Persist to `trust_scores.components` (stays, mean sub-scores, penalties, prior, decay half-life). The UI "Why this score?" drawer renders this JSON in plain language.

---

## 13. Decorator Module & Matching Engine (NV-2)

### 13.1 Decorator lifecycle
Register (role DECORATOR) → create profile (business name, area/base location, service radius, portfolio photos) → add **packages** → Admin approves → appears in directory and matching. Decorators manage blackout dates and respond to enquiries.

### 13.2 Package model
`event_types` (or `["ANY"]`), `theme_tags` (controlled vocabulary: `balloon, floral, traditional, minimal, kids, cartoon, festive, corporate, elegant, rustic, lights, stage_backdrop`), `layout_types` (or `["ANY"]`), capacity range `[min_capacity, max_capacity]`, `base_price`, `price_per_guest`, `setup_minutes`, `teardown_minutes`.

### 13.3 Match context
`MatchContext { hall{capacitySeated, capacityStanding, layoutType, lat, lng, powerPoints}, eventType, themes[], guestCount, budgetMin?, budgetMax?, slotStart, slotEnd }`

Entry points: `GET /bookings/{id}/decorator-matches` (post-lock/post-booking) and `GET /halls/{id}/decorator-matches?date=&eventType=&guests=&themes=` (hall-page teaser, no booking yet).

### 13.4 Algorithm

**Step 1 — Hard filters** (exclude; keep counts for the "why nothing matched?" hint):
| # | Filter |
|---|--------|
| F1 | Decorator `APPROVED`; package `active` |
| F2 | `haversine(hall, decorator.base) ≤ service_radius_km` |
| F3 | Package `event_types` contains the event type or `"ANY"` |
| F4 | Space size `S = hall.capacity_seated` satisfies `0.8·min_capacity ≤ S ≤ 1.25·max_capacity` |
| F5 | No decorator blackout and no other accepted enquiry overlapping `[slotStart − setup, slotEnd + teardown]` |

**Step 2 — Sub-scores (each 0..1):**
```
capacityFit  = 1 if min ≤ S ≤ max else linear decay to 0 at the 0.8·min / 1.25·max bounds
layoutFit    = 1 if layout_types ∋ hall.layout or "ANY"; else 0
themeFit     = no themes requested ? 0.5 : 0.75·coverage + 0.25·jaccard
                 coverage = |requested ∩ package| / |requested| ;  jaccard = |∩| / |∪|
price        = base_price + price_per_guest × guestCount
budgetFit    = budgetMax == null ? 0.7
             : price ≤ budgetMax ? (price ≥ budgetMin ? 1.0 : 0.9)
             : max(0, 1 − 2·(price − budgetMax)/budgetMax)
proximityFit = 1 − min(1, distanceKm / service_radius_km)
qualityFit   = trust_score/100 if present else 0.6   // new vendors flagged "New"
```

**Step 3 — Weighted total:**
```
score = 100 × ( 0.20·capacityFit + 0.10·layoutFit + 0.25·themeFit
              + 0.20·budgetFit  + 0.10·proximityFit + 0.15·qualityFit )
```
Weights are constants in `MatchWeights` (config-overridable; unit-tested to sum to 1.0).

**Step 4 — Per decorator, keep the best package**; expose `alternativePackages` count. Sort by score desc, tie-break by proximity then trust. Return top `limit` (default 10).

**Step 5 — Setup feasibility (soft):** check the hall's cells in `[slotStart − setup_minutes, slotStart)` are free. If not, set `warnings += "Setup would have to happen inside your booked time"`.

**Step 6 — Explanations (explainable output):** for every sub-score ≥ 0.8 add a positive reason; for ≤ 0.5 add a warning. Reason templates (localised):
- capacity: "Fits {min}–{max} guests — right for this hall"
- theme: "Matches {themes}"
- budget: "₹{delta} under budget" / "₹{delta} over budget"
- proximity: "{km} km away"
- quality: "Trust {score}" / "New vendor"

**Response item:**
```json
{ "decoratorId":"…","businessName":"Rang Decor","packageId":"…","packageName":"Kids Balloon Bash",
  "score":93,"priceEstimate":5100,"distanceKm":2.0,"trust":{"score":82,"badge":"STANDARD"},
  "reasons":["Fits 40–80 guests — right for this hall","Matches balloon, kids","₹900 under budget"],
  "warnings":[],"alternativePackages":1,
  "breakdown":{"capacity":1,"layout":1,"theme":0.92,"budget":1,"proximity":0.8,"quality":0.82} }
```
*Worked example:* hall seats 60 (open hall), birthday, themes `balloon,kids`, 40 guests, budget ₹4,000–6,000. Package: 40–80 capacity, themes `balloon,kids,cartoon`, base ₹3,500 + ₹40/guest = ₹5,100, 2 km of a 10 km radius, trust 82.
`themeFit = 0.75·1 + 0.25·0.667 = 0.917` → `0.20 + 0.10 + 0.229 + 0.20 + 0.08 + 0.123 = 0.932` → **score 93**.

### 13.5 Enquiry workflow
1. Renter `POST /decorator-enquiries {bookingId, decoratorId, packageId, message}` (booking must be `CONFIRMED` or `PENDING_PAYMENT`) → `SENT`; decorator notified. Match score + explanation snapshot stored.
2. Decorator `POST /vendor/enquiries/{id}/respond {accept, quotedPrice, note}` within 48 h → `ACCEPTED` / `DECLINED` / auto `EXPIRED`.
3. Renter `POST /decorator-enquiries/{id}/confirm` → `CONFIRMED_BY_RENTER`; system computes windows: `setup_window_start = start − setup_minutes`, `teardown_window_end = end + teardown_minutes`.
4. Reserve leading **SETUP cells** (insert `booking_cells cell_type=SETUP` for free cells before `start`). If they are not free, the renter chooses: setup inside booked time, or pick another decorator.
5. Issue a **DECORATOR credential** (§13.6). Payment to the decorator is **off-platform in v1** (quote is recorded).
6. Only one confirmed decorator per booking in v1.

### 13.6 Decorator Access Pass (N-04)
- Credential `kind=DECORATOR`, `valid_from = setup_window_start − 15 min`, `valid_until = teardown_window_end`.
- Delivered to the decorator's app/email as a QR (same token format, `k:"D"`).
- **Scan behaviour:** booking must be `CONFIRMED`/`CHECKED_IN` and pass in window → immediate **GO** (no OTP). Watchman sees: "**DECORATOR** · {business} · for {bookingRef} · renter {firstName}". Logs `DECORATOR_IN` / `DECORATOR_OUT` (second scan). Renter + owner get in-app notice "Decorator arrived".
- Hall-level option `require_renter_approval_for_decorator` (default off): scan returns HOLD until the renter approves in-app (2-minute timeout).
- Pass is revoked automatically if the booking is cancelled or the enquiry is cancelled.

---

## 14. Discovery & Search

### 14.1 Endpoint
`GET /api/v1/halls/search` — params: `lat`, `lng` (required unless `locality` given), `radiusKm` (default 5, max 25), `date`, `startTime`, `durationMinutes`, `guests`, `minPrice`, `maxPrice`, `amenities` (csv: `ac,parking,kitchen,stage,powerBackup,washroom`), `indoor`, `layout`, `minTrust`, `sort` (`distance|price|trust|rating`), `page`, `size`.

### 14.2 Query strategy (ADR-004)
1. Compute bounding box: `dLat = r/111.0`, `dLng = r/(111.0·cos(radians(lat)))`.
2. Native SQL with box prefilter (uses `ix_hall_status_geo`) + Haversine:
```sql
SELECT h.id,
       (6371 * ACOS(LEAST(1, COS(RADIANS(:lat)) * COS(RADIANS(h.lat)) * COS(RADIANS(h.lng) - RADIANS(:lng))
                            + SIN(RADIANS(:lat)) * SIN(RADIANS(h.lat))))) AS distance_km
FROM halls h
WHERE h.status = 'ACTIVE'
  AND h.lat BETWEEN :minLat AND :maxLat AND h.lng BETWEEN :minLng AND :maxLng
  AND h.capacity_standing >= :guests
  AND (:hasWindow = 0 OR NOT EXISTS (
        SELECT 1 FROM booking_cells c JOIN bookings b ON b.id = c.booking_id
        WHERE c.hall_id = h.id AND c.cell_start >= :winStart AND c.cell_start < :winEndPlusBuffer
          AND NOT (b.status = 'PENDING_PAYMENT' AND b.lock_expires_at < :now)))
HAVING distance_km <= :radius
ORDER BY distance_km
LIMIT 200;
```
3. Service post-filters the ≤ 200 candidates in Java for opening hours, blackouts, quiet hours, `latest_end_time`, slot-length rules, price and amenities, then sorts/paginates and computes `priceEstimate` for the requested window.
4. **Nearest-suitable ranking (default sort):** `distance` with a small boost for higher trust: `rankScore = distanceKm × (1.15 − 0.3 × trust/100)`.

### 14.3 Locality lookup
Migration `V10__localities.sql`: `localities(id, name, city, lat, lng)` seeded with ~50 Pune localities (dev) → `GET /geo/localities?q=`. Browser geolocation ("Near me") is optional and consent-based; nothing is stored unless the user saves a home location.

### 14.4 Alternatives (N-06 hook)
`GET /halls/{id}/alternatives?start=&durationMinutes=` returns: (a) nearest free start times same hall ±3 h and next 3 days; (b) other halls within 3 km free at that time, ranked by §14.2. Used by the 409 handler and by the "Slot taken" UI.

---

## 15. Owner Dashboard, Analytics & Smart Pricing Advisor

### 15.1 Metrics (period selectable; per hall or all halls)
| Metric | Definition |
|--------|------------|
| Bookings | Count by status |
| Occupied hours | Σ scheduled hours of bookings with status `CHECKED_IN/OUT/COMPLETED` |
| Available hours | Open-hours minus blackouts in the period |
| **Occupancy %** | occupied / available |
| Gross revenue | Σ `price_total` (captured, not refunded) |
| Owner earnings | Σ `(price_base − member_discount)` net of refunds (platform fee & tax shown separately) |
| Cancellation rate / No-show rate | share of bookings by status |
| Avg lead time | mean(start_at − created_at) |
| Repeat renter rate | renters with ≥ 2 bookings / total renters |
| Avg headcount vs declared | from check-in/peak headcount |
| Rating & trust | from §12 |
| Live status | `hall_live_status` (Free / Occupied / Cleaning) + next booking |

### 15.2 Usage heatmap
For the last 8 weeks, for each `(weekday, hour)`: `bookedShare = bookedCellHours / openCellHours`. Rendered as a 7 × 17 grid (7 am–12 am).

### 15.3 Smart Pricing Advisor (N-05)
Nightly job, per hall, per `(weekday, 2-hour block)` within open hours, using the last 8 weeks (≥ 6 observations):
```
occ < 0.20  → suggest DISCOUNT: pct = 5 × round( min(25, (0.20 − occ)/0.20 × 25) / 5 )   // 5–25%
occ > 0.80  → suggest SURGE: +10%
```
Stored in `pricing_suggestions` with rationale JSON. Owner taps **Apply** → creates a `hall_price_rules` row (high priority) or **Dismiss**. Advisory only; never auto-applied. Shows estimated extra monthly revenue (`suggestedDiscountedRate × expectedExtraBookings` — label clearly as an estimate).

---

## 16. Admin Panel & Disputes

### 16.1 Admin features
- **Approvals queue:** societies, halls, decorators — view submitted details/docs, approve or reject with reason (notification sent). Every action → `audit_log`.
- **Users:** search; view roles/status/KYC status/trust; suspend/reactivate; revoke KYC; reset watchman password (via owner).
- **Platform analytics:** users by role, active halls, bookings/day, GMV, funnel (lock → paid), KYC success rate, dispute rate, **median gate verification time** (`SCAN(HOLD)` → `CHECK_IN`), offline check-in share.
- **Health:** counts of PENDING outbox, dead-letter notifications, failed webhooks, job last-run times.

### 16.2 Disputes workflow
- Either side may raise within **72 h** after check-out (categories: damage, overstay, no access, misrepresented listing, cleanliness, other) with description, optional claimed amount, evidence.
- Status flow: `OPEN → UNDER_REVIEW → RESOLVED_FOR_RAISER | RESOLVED_AGAINST_RAISER | PARTIAL | WITHDRAWN`.
- Admin sees a single **evidence view**: timeline, scheduled vs actual, headcount, BEFORE/AFTER checklists and photos, entry logs, chat-free statements from both sides. Both parties can add evidence until resolution.
- Outcomes: note only · upheld dispute (feeds trust penalty `D`) · **partial/full refund** to the renter via gateway. Monetary settlement between owner and renter beyond refunds is **manual/off-platform in v1**; the resolution note is recorded.
- Booking `dispute_open = 1` while unresolved (blocks ratings-window auto-close for that booking).

---

## 17. Notifications

### 17.1 Mechanics
Business code calls `notificationService.enqueue(templateCode, recipient, payload, channels, dedupeKey?)` **inside the same transaction**; rows land in `notification_outbox`. `OutboxDispatcher` (every 5 s) picks `PENDING` rows with `next_attempt_at ≤ now`, renders the template in the recipient's language, sends via the channel adapter, marks `SENT` or schedules a retry (backoff 1 m → 5 m → 30 m → 2 h; then `DEAD`). **IN_APP** always creates a `notifications` row immediately. `dedupe_key` (e.g., `REMINDER_24H:{bookingId}`) prevents duplicates.

### 17.2 Adapters
`EmailChannel` (Spring Mail; HTML via Thymeleaf; QR PNG inline via ZXing for `QR_READY`) · `SmsChannel` → `SmsProvider` (`ConsoleSmsProvider` logs masked messages in dev; a real provider needs sender/template registration — DLT in India) · `InAppChannel` · (`PushChannel` P3).

### 17.3 Template catalogue

| Code | Trigger | Recipient | Channels |
|------|---------|-----------|----------|
| `OTP_SEND` (direct, not outboxed for latency) | verify / gate | user | SMS (+email fallback) |
| `BOOKING_CONFIRMED` | payment verified | renter; owner | EMAIL+SMS+IN_APP; IN_APP+EMAIL |
| `QR_READY` | QR issued | renter | EMAIL (QR inline) + IN_APP |
| `PAYMENT_RECEIPT` | captured | renter | EMAIL |
| `LOCK_EXPIRING` | 2 min left | renter | IN_APP |
| `BOOKING_CANCELLED`, `REFUND_PROCESSED` | cancel / refund | renter; owner | EMAIL+IN_APP (+SMS) |
| `REMINDER_24H`, `REMINDER_2H` | scheduler | renter | EMAIL(24h) / SMS+IN_APP |
| `WATCHMAN_TODAY` | 07:00 daily | watchman | IN_APP |
| `CHECKED_IN`, `CHECKED_OUT` | gate | owner; renter | IN_APP |
| `CAPACITY_ALERT` | headcount over limit | owner | IN_APP+SMS |
| `WRAP_UP_15`, `SLOT_ENDED`, `OVERSTAY` | scheduler | renter; watchman; owner | SMS+IN_APP; IN_APP |
| `OFFLINE_CHECKIN`, `SYNC_CONFLICT` | sync | owner | IN_APP+EMAIL |
| `RATING_REQUEST` | check-out (+24 h) | renter; watchman/owner | IN_APP+EMAIL |
| `DISPUTE_RAISED`, `DISPUTE_UPDATED` | dispute | counterparty; admin | EMAIL+IN_APP |
| `DECORATOR_ENQUIRY`, `ENQUIRY_RESPONSE`, `DECORATOR_PASS`, `DECORATOR_ARRIVED` | enquiry flow | decorator / renter / owner | EMAIL+IN_APP |
| `WAITLIST_OFFER` | slot freed | waitlisted user | SMS+IN_APP+EMAIL |
| `KYC_VERIFIED`, `KYC_EXPIRING` | KYC | user | IN_APP (+EMAIL) |
| `*_APPROVED / *_REJECTED` | admin | owner / decorator | EMAIL+IN_APP |
| `MEMBER_REQUEST`, `MEMBER_APPROVED` | membership | owner / resident | IN_APP |

SMS bodies ≤ 160 chars (English) / ≤ 70 chars Devanagari-safe; all templates exist in `en`, `hi`, `mr` (`messages_{lang}.properties` + email templates).

---

## 18. Scheduled Jobs

All jobs are idempotent and safe to re-run. Single-node uses Spring `@Scheduled(fixedDelay)`; if scaled out later, add ShedLock.

| Job | Cadence | Logic |
|-----|---------|-------|
| `LockExpiryJob` | 30 s | `PENDING_PAYMENT AND lock_expires_at < now` → `EXPIRED`, delete cells (any type), revoke nothing (no credential yet), notify, run waitlist promotion for freed windows. Also enqueue `LOCK_EXPIRING` at T-2 min |
| `ReminderJob` | 5 min | Bookings `CONFIRMED` starting in ~24 h / ~2 h → enqueue with dedupe key |
| `WrapUpOverstayJob` | 1 min | For `CHECKED_IN`: at `end−15` enqueue `WRAP_UP_15`; at `end` enqueue `SLOT_ENDED`; from `end+10` and every 15 min enqueue `OVERSTAY`, updating `overstay_minutes` and (if configured) accruing overstay fee note |
| `NoShowJob` | 5 min | `CONFIRMED AND end_at + grace < now` → `NO_SHOW`; revoke credentials; count against renter (N) |
| `RatingWindowJob` | 1 h | `CHECKED_OUT` with window closed or both rated → `COMPLETED`; skip if `dispute_open` |
| `TrustRecomputeJob` | nightly 02:00 | Recompute all subjects touched in last 24 h; full recompute weekly |
| `PricingAdvisorJob` | nightly 03:00 | §15.3 |
| `OutboxDispatcher` | 5 s | §17.1 |
| `KycExpiryJob` | daily | Flag `EXPIRED`; warn 14 days before |
| `DecoratorEnquiryExpiryJob` | 15 min | `SENT` older than 48 h → `EXPIRED` |
| `HallStatusJob` | 1 min | Move `CLEANING → FREE` after buffer; `OCCUPIED → …` sanity repair |
| `RetentionJob` | daily | Purge OTP rows > 7 days, expired refresh tokens > 30 days, idempotency keys > 7 days |


---

## 19. Added Novelty Features — Build Specs

> Build these only after the P0 flow is green end-to-end (`TASKS.md` Phases 15–20). Each has its own tests and feature flag (`app.features.<key>`), default **on** in dev.

### N-01 Offline-Resilient Watchman PWA → see §10.4
Flag `offlineGate`. Deliverables: manifest endpoint, IndexedDB store, offline verifier (`lib/qr-verify.ts`), outbox + sync endpoint, UI states, tests (airplane-mode Playwright test, sync idempotency, conflict case).

### N-02 Live Capacity Guard
Flag `capacityGuard`.
- **Levels** for a `CHECKED_IN` booking with `declared = guest_count`:
  | Level | Condition | Effect |
  |-------|-----------|--------|
  | OK | count < declared | green |
  | NOTICE | count ≥ declared | amber chip |
  | WARN | count ≥ ceil(1.25 × declared) | amber + owner in-app alert |
  | CRITICAL | count > `hall.capacity_standing` | red + owner SMS/in-app; watchman prompt "Hold entry, call owner"; verdict `HOLD(CAPACITY_EXCEEDED)` for new arrivals |
- `POST /entry/headcount {bookingId, count}` (absolute count; the stepper sends the new absolute value) updates `hall_live_status.current_headcount`, `bookings.peak_headcount`, and logs `HEADCOUNT`; alerts fire **on level change only** (dedupe).
- Counts only — **no faces, no per-person tracking**. Owner analytics: declared vs peak vs capacity.

### N-03 Quiet-Hours & Overstay Guard
Flag `quietGuard`.
- **Prevention:** rules 8 & 9 in §6.2; hall page and checkout show a banner ("Music must stop by 10:00 pm").
- **During event:** `WrapUpOverstayJob` (§18) sends `WRAP_UP_15` to renter (SMS + in-app) and watchman; `SLOT_ENDED` at end; `OVERSTAY` from end+10.
- **Recording:** `overstay_minutes` set at check-out; if `overstay_fee_per_15min > 0`: `fee = overstay_fee_per_15min × ceil(overstayMinutes/15)` written to the `CHECKED_OUT` event detail and evidence PDF. **Collection is off-platform in v1** (owner settles).
- Overstay feeds the renter's punctuality `p_i` (§12.3).

### N-04 Decorator Access Pass + setup/teardown buffers → see §13.5–13.6
Flag `decoratorPass`.

### N-05 Resident-Priority Windows, Community Pricing & Pricing Advisor → §6.8, §15.3
Flag `communityPricing`. Owner UI: "Society members" tab (approve/remove), priority-window and discount fields; resident UI: "Join society" request, member badge on hall page (“Member price ₹…”). Pricing Advisor cards in Owner Insights.

### N-06 Waitlist & Smart Alternatives
Flag `waitlist`.
- **Alternatives:** §14.4; surfaced automatically on `409 SLOT_UNAVAILABLE`.
- **Waitlist:** `POST /waitlist {hallId, start, durationMinutes, guests, eventType}` (max 3 active per user).
- **Promotion** (`WaitlistService.promote(hallId, freedWindow)` — triggered by expiry, cancellation, blackout removal): FIFO over `WAITING` entries fully inside the newly free window; run the same validation as booking creation; create a system-initiated `PENDING_PAYMENT` booking with **15-minute** lock; entry → `OFFERED`; send `WAITLIST_OFFER`. If paid → `CONVERTED`; if lock expires → entry `EXPIRED` and promotion continues with the next in queue.

### N-07 Handover Evidence Pack → §11
Flag `evidencePack` (BEFORE photos + PDF; AFTER checklist is core).

### N-08 Multilingual, low-literacy UI
Flag `i18n` (always on).
- `react-i18next` namespaces: `common, auth, search, booking, kyc, watchman, owner, decorator, admin, errors`. Keys are semantic (`booking.lock.heldFor`), never sentence-as-key. Missing key falls back to `en` and is logged in dev.
- Language order: user profile → `lang` preference (the only non-sensitive value allowed in `localStorage`) → browser.
- Formatting via `Intl` (`en-IN`, `hi-IN`, `mr-IN`); ₹ Indian grouping; all times IST.
- **Error codes are stable strings**; the UI maps `code → errors.<code>` localised text (server `message` is a fallback).
- Emails/SMS templates in all three languages (§17).
- Watchman: icon-first; optional spoken verdict using `speechSynthesis` (`hi-IN`/`mr-IN`/`en-IN`) when available — silent fallback.
- Add a CI check that all keys in `en` exist in `hi` and `mr` (missing = warning in P2, error before release).

### N-09 Privacy Dashboard
Flag `privacyDashboard`.
- `GET /me/privacy` → consents (purpose, version, granted/revoked), KYC summary (status, provider, masked suffix, verified date), data categories held and retention.
- `POST /me/consents/{purpose}/revoke` (KYC revoke also revokes outstanding credentials and blocks new bookings until re-verified).
- `GET /me/export` → JSON bundle: profile, bookings, ratings given/received (own), notifications, consents. **Excludes other users' personal data.**
- `DELETE /me` → allowed only with no active bookings/disputes; sets `DELETED`, anonymises name/email/phone to non-reversible placeholders, revokes tokens; financial/booking rows retained with anonymised renter for legal/accounting integrity.
- Retention table displayed to the user (e.g., OTPs 7 days, notifications 12 months, audit logs per policy).

### N-10 Split-the-Cost (P3)
Flag `splitPay`. Renter adds co-hosts (name, optional phone, share). System creates per-cohost payment links (`pay_token`); the booking lock is extended once to 30 min while shares are collected; if all shares paid → `CONFIRMED`; else `EXPIRED` and every paid share refunded automatically. Renter remains the QR holder.

### N-11 Natural-language event brief (P3)
Flag `nlBrief`. `POST /search/parse-brief {text}` → `{eventType, guests, date, startTime, durationMinutes, amenities[]}` via a `BriefParser` interface. Default `RuleBasedBriefParser` (keyword/regex for event types, "for 25 people", "Saturday 5–8"); optional LLM-backed parser behind config. The parsed filters are shown as **editable chips** — never auto-submitted. No user text is stored.

### N-12 Web Push (P3)
Flag `webPush`. VAPID keys in env; `push_subscriptions` table (user, endpoint, keys, ua); service worker `push` handler; used for `LOCK_EXPIRING`, `WRAP_UP_15`, `DECORATOR_ARRIVED`, `WAITLIST_OFFER`.

---

## 20. REST API Contract

**Base:** `/api/v1` · JSON · UTF-8 · timestamps ISO-8601 UTC (`2026-10-24T11:30:00Z`) · ids are public UUIDs · auth `Authorization: Bearer <access>` unless "public".
**Idempotency:** `Idempotency-Key` header (≤ 80 chars) accepted on `POST /bookings`, `POST /payments/orders`, `POST /payments/verify`, `POST /entry/checkout`.
**Pagination:** `?page=0&size=20&sort=field,dir` → `{content[], page, size, totalElements, totalPages}`.

### 20.1 Error shape & codes
```json
{ "timestamp":"2026-10-24T11:30:00Z", "status":409, "code":"SLOT_UNAVAILABLE",
  "message":"That slot was just taken.", "details":[{"field":null,"issue":"..."}],
  "traceId":"b1c2d3", "alternatives":[ ... ] }
```
| HTTP | Codes |
|------|-------|
| 400 | `VALIDATION_ERROR`, `INVALID_SLOT`, `PAYMENT_SIGNATURE_INVALID`, `OTP_INVALID` |
| 401 | `UNAUTHORIZED`, `TOKEN_EXPIRED` |
| 403 | `FORBIDDEN`, `KYC_REQUIRED`, `RATING_NOT_ELIGIBLE`, `OTP_LOCKED` |
| 404 | `NOT_FOUND` |
| 409 | `SLOT_UNAVAILABLE`, `ILLEGAL_STATE_TRANSITION`, `AMOUNT_MISMATCH`, `IDEMPOTENCY_CONFLICT`, `ALREADY_EXISTS` |
| 410 | `BOOKING_EXPIRED` |
| 422 | `TOO_SOON`, `OUTSIDE_BOOKING_WINDOW`, `OUTSIDE_OPEN_HOURS`, `HALL_BLOCKED`, `AFTER_LATEST_END`, `QUIET_HOURS`, `OVER_CAPACITY`, `TOO_MANY_LOCKS`, `HALL_NOT_AVAILABLE` |
| 429 | `RATE_LIMITED` |
| 500 | `INTERNAL_ERROR` |
Entry scan/verify **do not** use HTTP errors for verdicts — they return `200` with `verdict` and `reasonCode` (a STOP is a normal outcome); auth/format problems still use the codes above.

### 20.2 Endpoints

**Auth & profile**
| Method | Path | Access | Notes |
|--------|------|--------|-------|
| POST | `/auth/register` | public | `{fullName,email,phone,password,role(RESIDENT|HALL_OWNER|DECORATOR),language}` |
| POST | `/auth/otp/send` | public/auth | `{purpose,target}` rate-limited |
| POST | `/auth/otp/verify` | public/auth | `{challengeId,code}` |
| POST | `/auth/login` | public | sets refresh cookie |
| POST | `/auth/refresh` | cookie | rotates |
| POST | `/auth/logout` | auth | revokes family |
| POST | `/auth/forgot-password`, `/auth/reset-password`, `/auth/change-password` | mixed | |
| GET | `/me` | auth | profile + roles + kyc status + trust |
| PATCH | `/me` | auth | name, language |
| POST | `/me/photo` | auth | multipart |

**KYC & privacy** — `/kyc/consent`, `/kyc/start`, `/kyc/complete`, `/kyc/status`, `/kyc/revoke` (RESIDENT) · `/me/privacy`, `/me/consents/{purpose}/revoke`, `/me/export`, `DELETE /me` (auth)

**Societies & members**
| Method | Path | Access |
|--------|------|--------|
| GET | `/societies?q=` | auth |
| POST | `/societies/{id}/join-requests` | RESIDENT |
| POST | `/owner/societies` · GET `/owner/societies` | HALL_OWNER |
| GET | `/owner/societies/{id}/members?status=` | owner of society |
| POST | `/owner/societies/{id}/members/{userId}/approve` · `/remove` | owner of society |

**Halls (owner)**
| Method | Path | Notes |
|--------|------|-------|
| POST/GET | `/owner/halls` | create / list own |
| GET/PUT | `/owner/halls/{id}` | |
| POST | `/owner/halls/{id}/submit` | → `PENDING_APPROVAL` |
| POST/DELETE | `/owner/halls/{id}/photos[/{photoId}]` | multipart |
| PUT | `/owner/halls/{id}/opening-hours` | replace weekly set |
| POST/DELETE | `/owner/halls/{id}/blackouts[/{bid}]` | may trigger owner-cancel flow if bookings exist (must confirm) |
| PUT | `/owner/halls/{id}/price-rules` | replace set |
| POST/GET/DELETE | `/owner/halls/{id}/staff[/{userId}]` | create watchman `{fullName,phone,email?,tempPassword}` |
| GET | `/owner/halls/{id}/bookings` | filters: status, date range |
| POST | `/owner/bookings/{id}/cancel` | reason required; full refund |
| GET | `/owner/halls/{id}/live-status` | Free/Occupied/Cleaning + headcount |

**Discovery (public/auth)**
`GET /halls/search` · `GET /halls/{id}` · `GET /halls/{id}/availability` · `GET /halls/{id}/reviews` · `GET /halls/{id}/alternatives` · `GET /halls/{id}/decorator-matches` · `GET /geo/localities?q=`

**Bookings & payments (RESIDENT)**
| Method | Path | Notes |
|--------|------|-------|
| POST | `/bookings/quote` | price preview, no lock |
| POST | `/bookings` | **locks slot**; `Idempotency-Key` |
| GET | `/bookings` | mine (filters: status, upcoming/past) |
| GET | `/bookings/{id}` | holder / owner-of-hall / staff / admin |
| POST | `/bookings/{id}/cancel` | returns refund preview when `?preview=true` |
| PATCH | `/bookings/{id}/guest-count` | until T-24h |
| GET | `/bookings/{id}/qr` | `{token, validFrom, validUntil, maskedPhone}` |
| GET | `/bookings/{id}/timeline` | booking_events |
| GET | `/bookings/{id}/receipt.pdf` · `/bookings/{id}/evidence.pdf` | |
| POST | `/payments/orders` | `{bookingId}` → `{orderId, amount, currency, keyId}` |
| POST | `/payments/verify` | `{orderId, paymentId, signature}` → booking + QR |
| POST | `/payments/webhook` | public; HMAC-verified; idempotent |
| POST | `/bookings/{id}/decorator-approval` | renter approves/denies decorator entry (if option on) |

**Entry & watchman (WATCHMAN/HALL_OWNER)**
| Method | Path | Notes |
|--------|------|-------|
| GET | `/entry/public-keys` | |
| GET | `/entry/manifest` | offline manifest |
| GET | `/entry/today` | today's bookings for assigned halls |
| POST | `/entry/scan` | §10.1 |
| POST | `/entry/verify-otp` | §10.1 |
| POST | `/entry/reentry` | `{bookingId, confirm:true}` |
| POST | `/entry/headcount` | `{bookingId,count}` |
| POST | `/entry/before-report` | optional BEFORE checklist/photos |
| POST | `/entry/checkout` | §10.2 |
| POST | `/entry/sync` | §10.4 |

**Ratings & trust**
`GET /bookings/{id}/ratings/eligibility` · `POST /bookings/{id}/ratings` `{subjectType, dimensions{}, comment}` · `GET /trust/me` · `GET /halls/{id}/trust` (public summary) · `GET /decorators/{id}/trust`

**Disputes** — `POST /bookings/{id}/disputes` · `GET /disputes/{id}` · `POST /disputes/{id}/evidence` · `POST /disputes/{id}/withdraw`

**Decorators**
| Method | Path | Access |
|--------|------|--------|
| GET | `/decorators`, `/decorators/{id}` | public |
| POST/PUT | `/vendor/profile` | DECORATOR |
| POST/PUT/DELETE | `/vendor/packages[/{id}]` | DECORATOR |
| POST/DELETE | `/vendor/blackouts[/{id}]` | DECORATOR |
| GET | `/vendor/enquiries` | DECORATOR |
| POST | `/vendor/enquiries/{id}/respond` | DECORATOR |
| GET | `/bookings/{id}/decorator-matches` | RESIDENT (holder) |
| POST | `/decorator-enquiries` | RESIDENT |
| GET | `/decorator-enquiries?bookingId=` | RESIDENT |
| POST | `/decorator-enquiries/{id}/confirm` · `/cancel` | RESIDENT |
| GET | `/vendor/enquiries/{id}/pass` | DECORATOR (their QR pass) |

**Analytics & pricing (owner)** — `GET /owner/analytics/summary?hallId&from&to` · `/owner/analytics/heatmap` · `/owner/analytics/revenue-series` · `/owner/analytics/export.csv` · `GET /owner/pricing-suggestions` · `POST /owner/pricing-suggestions/{id}/apply|dismiss`

**Waitlist** — `POST /waitlist` · `GET /waitlist` · `DELETE /waitlist/{id}`

**Notifications** — `GET /notifications` · `GET /notifications/unread-count` · `POST /notifications/{id}/read` · `POST /notifications/read-all`

**Admin**
`GET /admin/approvals?type=society|hall|decorator` · `POST /admin/{societies|halls|decorators}/{id}/approve|reject` · `GET /admin/users` · `POST /admin/users/{id}/suspend|reactivate` · `POST /admin/users/{id}/revoke-kyc` · `GET /admin/disputes` · `GET /admin/disputes/{id}` · `POST /admin/disputes/{id}/resolve` · `GET /admin/analytics/overview` · `GET /admin/health/jobs`

### 20.3 Key payloads

**POST /bookings**
```json
// request
{ "hallId":"uuid", "startAt":"2026-10-24T11:30:00Z", "durationMinutes":180,
  "eventType":"BIRTHDAY", "eventTitle":"Aarav's 5th birthday", "themeTags":["balloon","kids"], "guestCount":25 }
// 201
{ "bookingId":"uuid","bookingRef":"SS-2610-7K3QF","status":"PENDING_PAYMENT",
  "lockExpiresAt":"2026-10-24T05:50:00Z","serverNow":"2026-10-24T05:40:00Z",
  "price":{"base":1350.00,"memberDiscount":135.00,"platformFee":60.75,"tax":229.64,"total":1505.39,"currency":"INR"},
  "isMemberBooking":true }
```

**POST /entry/scan** → `200`
```json
{ "verdict":"HOLD", "reasonCode":"OK",
  "booking":{"bookingRef":"SS-2610-7K3QF","hallName":"Green Meadows Hall","displayName":"Priya S.",
             "assurance":"HIGH","guestsExpected":25,"window":{"start":"…","end":"…"}},
  "otp":{"challengeId":"uuid","maskedPhone":"XXXXXX3210","expiresAt":"…"},
  "mode":"CHECK_IN" }
```
STOP example: `{"verdict":"STOP","reasonCode":"QR_NOT_YET_VALID","details":{"opensAt":"2026-10-24T11:00:00Z"}}`

**POST /entry/verify-otp** `{challengeId, otp, arrivedCount}` → `{"verdict":"GO","displayName":"Priya S.","endsAt":"…"}` or `{"verdict":"STOP","reasonCode":"OTP_FAILED","attemptsLeft":2}`

**POST /entry/sync**
```json
{ "deviceId":"dev-9f2c", "events":[
  {"clientEventId":"uuid","type":"CHECK_IN","jti":"…","occurredAt":"2026-10-24T11:41:07Z","identityMethod":"MANUAL_OFFLINE","headcount":18} ] }
// 200
{ "results":[{"clientEventId":"uuid","status":"ACCEPTED"}] }
```

**POST /payments/verify** `{orderId, paymentId, signature}` → `{ "booking":{…,"status":"CONFIRMED"}, "qr":{"token":"SS1.…","validFrom":"…","validUntil":"…"} }`

**Payment signature (Razorpay):** `HMAC_SHA256(orderId + "|" + paymentId, keySecret)` compared in constant time. Webhook: `HMAC_SHA256(rawBody, webhookSecret)` vs `X-Razorpay-Signature`.


---

## 21. Frontend Implementation

### 21.1 Routes & guards

| Path | Component | Access |
|------|-----------|--------|
| `/`, `/search`, `/halls/:id`, `/decorators`, `/decorators/:id`, `/how-it-works` | public pages | public |
| `/login`, `/register`, `/forgot-password` | auth | guest-only |
| `/kyc`, `/kyc/mock` (dev only) | KYC | RESIDENT |
| `/book/:hallId` | booking wizard | RESIDENT (+KYC) |
| `/bookings`, `/bookings/:id` | my bookings, detail + QR | RESIDENT |
| `/profile`, `/privacy`, `/notifications` | account | any auth |
| `/owner/**` | owner console | HALL_OWNER |
| `/watch/**` | watchman PWA | WATCHMAN (+HALL_OWNER manager) |
| `/vendor/**` | decorator console | DECORATOR |
| `/admin/**` | admin | ADMIN |

`<RoleRoute roles={[…]}>` reads roles from the `/me` query; unauthorised → `/login` or a 403 page. After login, redirect by primary role (`WATCHMAN → /watch`, `HALL_OWNER → /owner`, `DECORATOR → /vendor`, `ADMIN → /admin`, else `/`).

### 21.2 API client (`services/http.ts`)
- Thin `fetch` wrapper: base URL from `VITE_API_BASE_URL`, JSON in/out, attaches `Authorization`, `Accept-Language`, `X-Request-Id`.
- **401 handling:** one automatic `/auth/refresh` (cookie) then retry once; on failure clear auth state and go to `/login`.
- Adds `Idempotency-Key` (UUID) to the POSTs listed in §20.
- Maps error responses to a typed `ApiError { status, code, message, details, alternatives }`.
- Types generated from OpenAPI: `npm run gen:api` → `src/types/api.d.ts`.

### 21.3 Key screens — behaviour notes

**Search** — URL-synced filters (`?lat&lng&date&start&dur&guests…`). Debounced locality autocomplete → `/geo/localities`. "Near me" asks browser geolocation on click only. List ⇄ Map toggle (Leaflet, OSM attribution required). Each `HallCard` shows distance, capacity, ₹/hr, trust badge, availability for the requested window, member price if applicable.

**Hall detail** — gallery, amenities, rules, quiet-hours banner, price rules table, verified reviews, trust "Why this score?" drawer, availability calendar (`SlotGrid`), decorator teaser (top 3 from hall-level matches), sticky "Book" bar.

**Booking wizard (`/book/:hallId`)** — steps: (1) Slot, (2) Details (event type, title, guests, themes), (3) Review & price, (4) Pay. On Step 3 → "Continue" calls `POST /bookings` (lock). From then a sticky `LockCountdown` runs from `lockExpiresAt` using `serverNow` offset (compute `skew = serverNow − Date.now()` once). On expiry: friendly modal + "See nearby times". Refresh-safe: wizard state derives from `GET /bookings/:id` when `?booking=` present. On `409 SLOT_UNAVAILABLE` show the `alternatives` chips and a **Join waitlist** button.

**`SlotGrid`** — day view (30-min cells). Interaction: tap a start cell → choose duration chips (min…max) → selection highlights consecutive cells; invalid ranges disabled with tooltip reason (booked, quiet hours, closed). Keyboard: arrow keys move, Enter selects; each cell has `aria-label="Saturday 5:00 pm, available"`. Legend per `DESIGN.md` §4.

**Payment** — `PaymentGateway` front-end adapter: `RazorpayCheckout` (script loaded lazily on this page only) or `MockCheckout` (dev; simulates success/failure). After success: `POST /payments/verify` → navigate to `/bookings/:id?new=1` showing confetti-free, calm confirmation + QR.

**QR wallet (`/bookings/:id`)** — `QrCard` (`qrcode.react`, size ≥ 280, level M, high-contrast); text "Opens 4:30 pm · Closes 8:30 pm"; timeline; cancel (refund preview dialog); add decorator; directions link (`geo:`/OSM). The QR response is cached by the service worker (PII-free token) so it displays offline.

**Watchman** — see `DESIGN.md` §9. Components: `WatchHome`, `Scanner` (`html5-qrcode`, back camera, torch, manual fallback), `Verdict` (GO/HOLD/STOP), `OtpPad`, `Headcount`, `CheckoutForm` (checklist + camera capture), `OfflineBanner`, `SyncQueue`. Use the **Wake Lock API** while scanning. Verdict sound via `AudioContext` beeps (no audio files needed).

**Owner** — Dashboard (KPIs, revenue chart, heatmap, live status, pricing advisor), Halls CRUD wizard (Details → Photos → Hours & blackouts → Pricing → Rules → Staff → Members → Submit), Calendar control (block/unblock, view bookings), Bookings table (filters, cancel with reason), Disputes.

**Decorator console** — profile, packages editor (tag pickers), availability, enquiry inbox (accept + quote), QR pass view.

**Admin** — approvals queue with document viewer, users table, dispute detail (evidence view), analytics overview.

### 21.4 PWA (`vite-plugin-pwa`, strategy `injectManifest` for a custom service worker)
- Manifest: name "SmartSpace", short_name "SmartSpace", `display: standalone`, theme `#0F766E`, icons 192/512 (+ maskable), `start_url: "/"`, `scope: "/"`.
- SW: precache app shell; runtime caching — static assets `CacheFirst`; `GET /api/v1/entry/manifest` and `GET /api/v1/bookings/*/qr` → `NetworkFirst` (with timeout, then cache); images `StaleWhileRevalidate`. **Never cache** auth endpoints or any response containing OTPs.
- Background Sync tag `entry-sync` flushes the IndexedDB outbox (fallback: `online` event + manual "Sync now").
- Install prompt shown to watchmen after first successful login.

### 21.5 Offline verifier (`lib/qr-verify.ts`) — pseudocode
```ts
export function verifyOffline(token: string, manifest: Manifest, nowMs: number): Verdict {
  const parts = token.split(".");
  if (parts.length !== 3 || parts[0] !== "SS1") return stop("QR_MALFORMED");
  const payload = JSON.parse(utf8(b64urlDecode(parts[1])));
  const key = manifest.keys.find(k => k.kid === payload.i);
  if (!key) return stop("QR_UNKNOWN_KEY");
  const ok = ed25519.verify(b64urlDecode(parts[2]), utf8Bytes("SS1." + parts[1]), b64urlDecode(key.x));
  if (!ok) return stop("QR_INVALID_SIGNATURE");
  const cred = manifest.credentials.find(c => c.jti === payload.j);
  if (!cred) return stop("QR_UNKNOWN_OR_OTHER_HALL");
  if (manifest.revokedJtis.includes(cred.jti)) return stop("QR_REVOKED");
  const t = nowMs / 1000;
  if (t < payload.f) return stop("QR_NOT_YET_VALID", { opensAt: payload.f });
  if (t > payload.x) return stop("QR_EXPIRED");
  return hold("OK", { displayName: cred.displayName, guests: cred.guestsExpected, assurance: cred.assurance, offline: true });
}
```
Unit-test with fixed key pairs and edge times (nbf−1s, nbf, exp, exp+1s).

### 21.6 State & data
TanStack Query keys per feature (`['halls','search',params]`, `['booking',id]`, …); mutations invalidate precisely. Auth state (access token, user) in a small context (memory). Form state via RHF; schemas in `features/*/schemas.ts` (Zod) mirror backend validation. Global error boundary + toast for `ApiError`.

### 21.7 Definition of a finished screen
Follows `DESIGN.md`; loading/empty/error states; responsive at 375/768/1440; i18n keys in `en/hi/mr`; keyboard + axe pass; unit/component test for logic; covered by an E2E path if it is on a core journey.

---

## 22. Testing Strategy

### 22.1 Pyramid
| Level | Tooling | Focus |
|-------|---------|-------|
| Unit (backend) | JUnit 5, Mockito, fixed `Clock` | pricing, cancellation, state machine, rules validator, trust calculator, match scorer, QR token, OTP, cell generation |
| Integration (backend) | Spring Boot Test + **Testcontainers MySQL** | repositories, Flyway migrations, slot engine, booking→payment→QR flow, entry flow, RBAC/ownership, scheduler jobs |
| Architecture | ArchUnit | layering rules, no entity in controllers, no `Instant.now()` outside `Clock` |
| Unit/Component (frontend) | Vitest, RTL, MSW | SlotGrid, LockCountdown, verdict screens, offline verifier, i18n key coverage, forms |
| E2E | Playwright (+ axe) | journeys below, run against compose stack with mock KYC/payment |
| Security | Spring Security tests + checklist | authz matrix, IDOR, rate limits, token misuse, log masking |
| Non-functional smoke | k6 (optional) | search p95, scan p95 |

### 22.2 Must-have backend tests
1. **Concurrency:** 50 threads `reserve()` the same hall/slot → exactly 1 success, 49 `SlotUnavailableException`; cells table has exactly the expected rows.
2. **Overlap matrix:** adjacent (allowed), overlapping by one cell (rejected), buffer overlap rules, cross-hall independence.
3. **Lock expiry:** unpaid booking expires → cells freed → another user can book; payment arriving after expiry triggers refund path.
4. **Payment idempotency:** duplicate `verify` and duplicate webhook produce one `CONFIRMED` transition, one QR, one receipt.
5. **Signature:** invalid payment signature rejected; tampered QR signature rejected; QR outside window rejected; QR for a different hall rejected.
6. **OTP:** correct code passes; 3 wrong codes lock; expired code fails; code cannot be reused; resend cooldown.
7. **State machine:** every illegal transition throws; each legal transition writes exactly one `booking_events` row.
8. **Cancellation matrix:** each policy × each time band → expected refund; owner cancel → 100% incl. fee.
9. **Pricing:** rule priority, weekday/weekend, member discount, rounding.
10. **Trust score:** worked examples in §12 reproduce **72.6** and **73.9**; collusion down-weight; decay; badge boundaries.
11. **Decorator match:** worked example reproduces **93**; filters F1–F5; weights sum to 1.0; explanations generated.
12. **Authorization:** for each endpoint group, wrong role → 403, other user's booking → 404, watchman of another hall → `WRONG_HALL`.
13. **Verified-only ratings:** cannot rate without check-in/out; cannot rate twice; window enforced.
14. **Offline sync:** duplicate `clientEventId` → `DUPLICATE`; late sync for a cancelled booking → `SYNC_CONFLICT` + owner alert.
15. **Privacy:** KYC table never contains fields beyond spec (schema test); logs contain no OTP/QR/phone in clear.

### 22.3 E2E journeys (Playwright)
| ID | Journey |
|----|---------|
| E2E-01 | Register → verify (mock OTP) → mock KYC → search → book 3 h → pay (mock) → QR visible |
| E2E-02 | Two browsers race for the same slot → one wins, other sees alternatives |
| E2E-03 | Lock expiry: wait/advance clock → slot returns to free |
| E2E-04 | Watchman scan (injected QR) → HOLD → correct OTP → GO → booking CHECKED_IN → owner sees Occupied |
| E2E-05 | Screenshot-share: valid QR + wrong OTP ×3 → STOP/locked |
| E2E-06 | Check-out with checklist + photo → both sides rate → trust updates |
| E2E-07 | Owner creates hall → admin approves → hall appears in search |
| E2E-08 | Decorator matches → enquiry → accept → confirm → decorator pass → watchman scans DECORATOR |
| E2E-09 | Offline gate: go offline → scan → GO(offline) → back online → sync → owner sees notice |
| E2E-10 | Cancel with refund preview → refund recorded |
| E2E-11 | Dispute raised → admin resolves with evidence view |
| E2E-12 | Language switch EN→हिन्दी→मराठी on key screens; a11y (axe) on core pages; viewports 375/768/1440 |

### 22.4 Quality gates
Backend line coverage ≥ 70% on `*/service` and `*/domain`; frontend ≥ 60% on logic modules; **zero** high-severity axe violations on core pages; `npm audit` / dependency check has no unpatched critical issues; CI must be green to merge.

---

## 23. DevOps

### 23.1 Dockerfiles
**backend/Dockerfile**
```dockerfile
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -r -u 1001 appuser && mkdir -p /app/uploads && chown appuser /app/uploads
COPY --from=build /app/target/*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","app.jar"]
```
**frontend/Dockerfile** — Node 22 build stage → static files copied into the proxy image (Caddy or Nginx).

### 23.2 `docker-compose.yml` (prod-like)
```yaml
services:
  mysql:
    image: mysql:8.4
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    command: ["--character-set-server=utf8mb4","--collation-server=utf8mb4_0900_ai_ci","--default-time-zone=+00:00"]
    volumes: [ "mysql_data:/var/lib/mysql" ]
    healthcheck: { test: ["CMD","mysqladmin","ping","-h","localhost"], interval: 10s, timeout: 5s, retries: 10 }
  backend:
    build: ./backend
    env_file: .env
    depends_on: { mysql: { condition: service_healthy } }
    volumes: [ "uploads:/app/uploads", "./infra/keys:/run/keys:ro" ]
  proxy:
    build: { context: ., dockerfile: frontend/Dockerfile }   # builds SPA + Caddy/Nginx
    ports: [ "443:443", "80:80" ]
    depends_on: [ backend ]
volumes: { mysql_data: {}, uploads: {} }
```
`docker-compose.dev.yml` adds **MailHog** (SMTP 1025, UI 8025), Adminer, published MySQL/backend ports, and `SPRING_PROFILES_ACTIVE=dev` (mock KYC/payment/SMS, seed data).

### 23.3 HTTPS (required for phone camera scanning)
Caddy example (`infra/caddy/Caddyfile`):
```
smartspace.local {
  tls internal            # Caddy internal CA; install its root cert on watchman phones for LAN demos
  encode gzip
  handle /api/* { reverse_proxy backend:8080 }
  handle { root * /srv/www
           try_files {path} /index.html
           file_server }
}
```
Alternatives: `mkcert` for local; a real domain with Let's Encrypt; or a tunnel for demos. Note `localhost` is treated as a secure context by browsers for development.

### 23.4 CI (`.github/workflows/ci.yml`)
Jobs: **backend** (setup-java 21 → `./mvnw -B verify` with Testcontainers) · **frontend** (setup-node 22 → `npm ci` → `lint` → `typecheck` → `test` → `build`) · **e2e** (compose up → Playwright) · **security** (dependency audit, secret scan e.g. gitleaks). Cache Maven/npm. Fail on any error.

### 23.5 Operations
- Backups: `infra/scripts/backup.sh` → `mysqldump --single-transaction` + tar of uploads, nightly, keep 14 days; `restore.sh` documented and tested once.
- Migrations run automatically at backend start (Flyway); never run manual SQL on prod.
- Logs: JSON logs to stdout; correlation id; rotate via Docker `json-file` limits.
- Health: `/actuator/health/liveness`, `/readiness`; metrics on an internal-only port/path.
- Secrets: `.env` on the host with `chmod 600`; QR private key mounted read-only from `infra/keys/` (git-ignored). Rotation: add new key id `k2`, issue new tokens with `k2`, keep `k1` public key in `/entry/public-keys` until old tokens expire.

---

## 24. Seed Data & Demo Script

### 24.1 Dev seed (`db/seed`, dev profile only; all passwords from `SEED_PASSWORD`, default `Demo@12345`)
| Role | Email | Notes |
|------|-------|-------|
| Admin | `admin@smartspace.test` | |
| Owner | `owner.meadows@smartspace.test` | Green Meadows CHS |
| Owner | `owner.lakeview@smartspace.test` | Lakeview Residency |
| Watchman | `watch.meadows@smartspace.test` | assigned to Green Meadows Hall |
| Resident | `priya@smartspace.test` | KYC verified (mock), society member |
| Resident | `rahul@smartspace.test` | KYC **not** verified (tests `KYC_REQUIRED`) |
| Decorator | `rang.decor@smartspace.test` | 3 packages, approved |
| Decorator | `floral.studio@smartspace.test` | 2 packages, approved |

- **6 halls** across nearby localities (approximate demo coordinates: Kothrud 18.5074,73.8077 · Baner 18.5590,73.7868 · Aundh 18.5580,73.8075 · Hadapsar 18.5089,73.9259 · Viman Nagar 18.5679,73.9143 · Wakad 18.5975,73.7898) with varied capacity (20–120), layouts, amenities, price rules and quiet hours; all `ACTIVE` except one `PENDING_APPROVAL`.
- **~120 historical bookings** over 10 weeks with realistic distribution (weekend evenings busy, weekday mornings idle) so the heatmap and Pricing Advisor show meaningful output; includes check-in/out logs, ratings, two disputes, one overstay, one no-show.
- `localities` table with ~50 Pune localities.
- A `POST /dev/reset-demo` endpoint (dev profile only) re-seeds and can **fast-forward the `Clock`** for demoing expiry/reminders.

### 24.2 Demo walkthrough (10 minutes)
1. **Resident (Priya)** searches "Baner, Saturday 5–8 pm, 25 guests" → sees 3 halls, distance-sorted, member price.
2. Opens Green Meadows Hall → picks 3 h → **slot locks** (countdown) → open the same slot as Rahul in a private window → sees *Locked* → alternatives + waitlist.
3. Priya pays (mock) → **QR issued** + email in MailHog.
4. Decorator matches appear with "why matched" → sends enquiry → decorator accepts (second window).
5. **Event day** (fast-forward clock): Watchman scans Priya's QR → **HOLD** → OTP appears in console/MailHog SMS → enters → **GO**; owner dashboard flips to *Occupied*.
6. Watchman tries a **screenshot of the QR** on another phone → OTP goes to Priya's phone → **STOP**.
7. Turn on airplane mode: decorator pass scanned **offline** → GO(offline) → reconnect → sync.
8. Headcount stepper crosses declared → amber → capacity red → owner alert.
9. Wrap-up alert, overstay 12 min → check-out with checklist + photo.
10. Both rate; **trust scores** update with "Why this score?".
11. Owner insights: heatmap + Pricing Advisor suggestion → Apply.
12. Admin: approve pending hall; resolve a dispute using the evidence view.

---

## 25. Evaluation Plan, Risks & Future Work

### 25.1 Evaluation plan (supports reporting/publication)
| Question | Method | Metric |
|----------|--------|--------|
| Does the cell model prevent double-booking? | Concurrency test: k ∈ {10, 50, 200} threads × 100 rounds | double-booking count (target 0), p95 latency |
| Does dual-layer entry resist impersonation? | Scripted scenarios: screenshot share, forwarded QR, expired QR, wrong hall, OTP guessing | attack success rate vs QR-only baseline |
| Is the watchman flow fast and usable? | Task-timing study with 5–10 volunteers (incl. non-technical) | median scan→GO time, error rate, SUS score |
| Does offline mode hold up? | Airplane-mode test matrix (fresh manifest, stale manifest, revoked pass) | correct-verdict rate, sync-conflict handling |
| Is the trust score harder to game than plain stars? | Simulation: honest users vs colluding pairs vs burst reviews | score displacement vs plain average |
| Are decorator matches good? | Blind rating of top-3 by volunteers vs a popularity/random baseline | precision@3, NDCG@3 |
| Does pricing advice help? | Backtest on seeded occupancy | estimated occupancy/revenue uplift (clearly labelled as simulation) |

### 25.2 Risks & mitigations
| Risk | Mitigation |
|------|-----------|
| Legal limits on Aadhaar usage | Provider abstraction, mock in build, alternate ID routes, no raw storage, legal review before real use (`PRD.md` R1) |
| Watchman phone/camera issues | Manual code fallback, torch, clear errors, offline mode |
| Clock skew (device vs server) | Server time offset on client; ±5 min tolerance on sync; QR windows include grace |
| SMS deliverability/cost | Provider abstraction; email + in-app fallback; OTP also viewable in-app for the booker (owner-toggle) |
| Scope creep | Tier gates, `TASKS.md`, `RULES.md` |
| Data leakage via logs/uploads | Log sanitiser tests, authorised file serving, EXIF strip |

### 25.3 Future work (not in v1)
Automated owner payouts/settlement, deposit-free damage insurance partnerships, native apps, smart-lock/IoT gate integration (relay opens on GO), multi-city ops, decorator payments in-app, recommendation learning from booking outcomes, accessibility audit with real users, formal threat model and penetration test.
