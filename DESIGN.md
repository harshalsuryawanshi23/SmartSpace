# Design System

> **DESIGN.md = HOW IT SHOULD LOOK AND FEEL.** Every screen must follow this file so pages look like one product.

## 1. Design Principles

1. **Trust first.** This app handles identity and money. Calm colours, clear language, visible verification states.
2. **Three-tap clarity.** Common tasks (find → pick slot → pay) never exceed a few taps on a phone.
3. **Role-tuned density.** Residents: friendly and spacious. Owners/Admin: denser, data-forward. **Watchman: enormous, minimal, unmistakable.**
4. **Never colour alone.** Every status has an icon + label as well as a colour.
5. **Honest microcopy.** Say "verified identity", not "guaranteed identity". Explain *why* we ask for KYC.
6. **Local first.** English, हिन्दी, मराठी; IST times; ₹ formatting with Indian digit grouping.

## 2. Style

Modern · Minimal · Professional · Warm. Rounded cards, soft shadows, generous whitespace, one strong accent.

## 3. Typography

| Use | Font | Fallback |
|-----|------|----------|
| Latin UI | **Inter** | system-ui, -apple-system, Segoe UI, Roboto, sans-serif |
| Devanagari (HI/MR) | **Noto Sans Devanagari** | Mangal, sans-serif |
| Numbers / codes / OTP | **JetBrains Mono** (tabular) | ui-monospace, monospace |

Load via `@fontsource` packages (self-hosted; no external CDN dependency).

| Token | Size / Line height | Weight | Use |
|-------|--------------------|--------|-----|
| `display` | 36 / 44 | 700 | Hero, watchman verdict |
| `h1` | 28 / 36 | 700 | Page title |
| `h2` | 22 / 30 | 600 | Section title |
| `h3` | 18 / 26 | 600 | Card title |
| `body` | 16 / 24 | 400 | Default |
| `small` | 14 / 20 | 400 | Secondary text |
| `caption` | 12 / 16 | 500 | Labels, chips |

Minimum body size 16 px on mobile. Devanagari gets +2 px line-height.

## 4. Colour Tokens

### Brand & neutrals
| Token | Hex | Use |
|-------|-----|-----|
| `primary-600` | `#0F766E` | Primary buttons, links, active states (teal) |
| `primary-700` | `#115E59` | Hover/pressed |
| `primary-50` | `#F0FDFA` | Subtle backgrounds |
| `accent-500` | `#F59E0B` | Highlights, badges (use with dark text) |
| `bg` | `#F8FAFC` | App background |
| `surface` | `#FFFFFF` | Cards |
| `border` | `#E2E8F0` | Dividers, inputs |
| `text` | `#0F172A` | Primary text |
| `muted` | `#64748B` | Secondary text (≥ 4.5:1 on white) |

### Semantic (WCAG AA-safe with white text where noted)
| Token | Hex | Notes |
|-------|-----|-------|
| `success-700` | `#15803D` | White text OK |
| `success-50` | `#F0FDF4` | Tint background |
| `warning-700` | `#B45309` | White text OK |
| `warning-50` | `#FFFBEB` | Tint background |
| `danger-600` | `#DC2626` | White text OK |
| `danger-50` | `#FEF2F2` | Tint background |
| `info-600` | `#2563EB` | White text OK |
| `info-50` | `#EFF6FF` | Tint background |

### Availability calendar cell colours
| State | Fill | Pattern/Icon |
|-------|------|--------------|
| Free | `success-50` + border `success-700` | ✓ (on hover: "Select") |
| Selected | `primary-600` (white text) | ● |
| Locked (someone else, ≤10 min) | `warning-50` | ⏳ |
| Booked | `#E2E8F0` | ▒ diagonal hatch |
| Blocked / closed / quiet hours | `#F1F5F9` | ✕ |

### Dark mode
Supported via `class="dark"` (Tailwind `darkMode: "class"`), following OS by default. Dark tokens: `bg #0B1220`, `surface #111A2E`, `border #24304A`, `text #E5E7EB`, `muted #94A3B8`; primary shifts to `#2DD4BF` for text/links (contrast ≥ 4.5:1 on `surface`).

## 5. Spacing, Radius, Elevation, Motion

- Spacing scale: 4 · 8 · 12 · 16 · 24 · 32 · 48 · 64.
- Radius: **cards 12 px**, buttons/inputs 10 px, chips 999 px, modals 16 px.
- Elevation: `shadow-sm` on cards, `shadow-lg` on modals/sheets.
- Motion: 150–200 ms ease-out; respect `prefers-reduced-motion` (disable non-essential animation). Lock countdown uses a linear progress bar.
- Breakpoints (Tailwind): `sm 640`, `md 768`, `lg 1024`, `xl 1280`. Test widths: **375 · 768 · 1440**.

## 6. Tailwind Config Sketch

```ts
// tailwind.config.ts
export default {
  darkMode: "class",
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: { 50: "#F0FDFA", 600: "#0F766E", 700: "#115E59" },
        accent: { 500: "#F59E0B" },
        success: { 50: "#F0FDF4", 700: "#15803D" },
        warning: { 50: "#FFFBEB", 700: "#B45309" },
        danger: { 50: "#FEF2F2", 600: "#DC2626" },
        info: { 50: "#EFF6FF", 600: "#2563EB" },
        ink: "#0F172A", muted: "#64748B", line: "#E2E8F0", canvas: "#F8FAFC",
      },
      fontFamily: {
        sans: ["Inter", "Noto Sans Devanagari", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "ui-monospace", "monospace"],
      },
      borderRadius: { card: "12px", control: "10px" },
    },
  },
};
```

## 7. Components

Build once in `src/components/` and reuse everywhere.

| Component | Variants / notes |
|-----------|------------------|
| **Button** | `primary`, `secondary` (outline), `ghost`, `destructive`; sizes `md`, `lg`, `xl` (watchman); loading spinner state; min touch target 44×44 px (watchman 64 px) |
| **Input / Select / DatePicker / TimeSlotPicker** | Label above, helper text, error text, required marker; RHF + Zod integration |
| **Card** | radius 12, `shadow-sm`, optional header/footer; `HallCard` shows photo, name, distance, capacity, price/hr, trust badge |
| **Chip / Badge** | Status chips with icon + text; amenity chips |
| **TrustBadge** | `NEW`, `TRUSTED`, `WATCH` with score ring (0–100) and tooltip "Built only from verified stays" |
| **StatusPill** | Booking status: Pending payment, Confirmed, Checked-in, Checked-out, Completed, Cancelled, Expired, No-show |
| **SlotGrid** | 30-min cell calendar per day with drag/tap selection, min/max duration enforced, colour states in §4 |
| **LockCountdown** | Sticky bar: "Slot held for 09:42" + progress |
| **PriceBreakdown** | Base, member discount, platform fee, tax, **Total** |
| **QrCard** | Big QR, booking ref, window, "Works offline", brightness-boost hint, "Add to Home Screen" prompt |
| **VerdictScreen** | Full-screen GO/HOLD/STOP (see §9) |
| **Timeline** | Vertical audit trail: Created → Paid → QR issued → Checked-in → Checked-out → Rated |
| **DecoratorMatchCard** | Score ring, "Why matched" chips, price estimate, CTA "Send enquiry" |
| **EmptyState / ErrorState / Skeleton** | Mandatory on every data view |
| **Toast / Dialog / BottomSheet** | Bottom sheet on mobile for filters and confirmations |
| **LanguageSwitcher** | EN · हिं · मरा in header and settings |
| **ConsentSheet** | Plain-language purpose, what is stored / not stored, "Agree" and "Not now" |

## 8. Information Architecture & Screens

### 8.1 Resident
`/` Home (search bar, "near me", quick event types) · `/search` (list + map toggle, filters sheet) · `/halls/:id` (gallery, info, calendar, reviews, decorators teaser) · `/book/:hallId` (slot → details → decorator (optional) → review → pay) · `/bookings` · `/bookings/:id` (timeline, QR, cancel, decorators, rate) · `/kyc` · `/profile` · `/privacy`

### 8.2 Hall Owner
`/owner` dashboard · `/owner/halls` · `/owner/halls/new` · `/owner/halls/:id/edit` (tabs: Details, Photos, Hours & Blackouts, Pricing, Rules, Staff, Members) · `/owner/calendar` · `/owner/bookings` · `/owner/insights` (heatmap, pricing advisor) · `/owner/disputes`

### 8.3 Watchman (PWA — separate lightweight layout, no side nav)
`/watch` home (today's list + big **Scan** button + online/offline pill) · `/watch/scan` · `/watch/verdict` · `/watch/booking/:id` (headcount, checkout) · `/watch/checkout/:id` (checklist + photos) · `/watch/queue` (offline sync queue)

### 8.4 Decorator
`/vendor` dashboard · `/vendor/profile` · `/vendor/packages` · `/vendor/availability` · `/vendor/enquiries`

### 8.5 Admin
`/admin` overview · `/admin/approvals` · `/admin/users` · `/admin/disputes` · `/admin/analytics`

### 8.6 Public
`/login` · `/register` · `/decorators` · `/decorators/:id` · `/how-it-works` · `/privacy-policy`

## 9. Watchman Design (Watchman-first — NV-5)

Guiding constraint: a non-technical guard, one hand, bright sunlight, possibly poor signal.

- **Layout:** single column, no menus. Bottom-fixed primary action, 64–72 px buttons, 18–20 px body text.
- **Home:** header pill (🟢 Online / 🟠 Offline · N queued), today's bookings as large cards (time, hall, holder first name, guests), one giant **SCAN QR** button.
- **Scanner:** full-screen camera, high-contrast frame, torch toggle, "Enter code manually" fallback.
- **Verdict screens (full-screen colour + icon + one line + sound/vibration):**

| Verdict | Colour | Icon | Copy (EN) | Extra |
|---------|--------|------|-----------|-------|
| **GO** | `success-700` | ✓ | "Let them in" | Shows verified name, guests expected, hall name; haptic + short beep |
| **HOLD** | `warning-700` | ✋ | "Ask for the code" | OTP keypad (6 big digits), masked phone, verified name to compare with ID |
| **STOP** | `danger-600` | ✕ | Plain reason e.g. "Not today's booking" / "Too early — opens 4:30 pm" / "Booking cancelled" | Reason code small at bottom; "Call owner" button |

- **Icon-first** copy; all strings localised. Optional audio prompt in Hindi/Marathi for the verdict.
- **Headcount:** `−` / `+` stepper with live "23 / 40 max". At 100% of declared guests: amber. Over hall capacity: red + owner alert (N-02).
- **Checkout:** 6 checkbox rows with icons (Lights off, Fans/AC off, Chairs/tables in place, Floor clean, No damage, Keys returned) + note field + up to 4 photos (camera capture). Big **Finish** button.
- **Offline:** amber banner; scan works; events queue; "Synced ✓" toast when back online.

## 10. Key Screen Wireframes (text)

### Search (mobile)
```
┌──────────────────────────────┐
│ ☰  SmartSpace        EN ▾  👤│
├──────────────────────────────┤
│ 📍 Near me / locality...   ⌕ │
│ [Sat 24 Oct ▾] [5–8 pm ▾]    │
│ [25 guests ▾]  [Filters ⚙ 2] │
├──────────────────────────────┤
│ [List]  [Map]                │
│ ┌──────────────────────────┐ │
│ │ [photo]                  │ │
│ │ Green Meadows Hall  ★ 92 │ │
│ │ 0.8 km · 40 seats · AC   │ │
│ │ ₹450/hr    ● Free 5–8 pm │ │
│ └──────────────────────────┘ │
│ ...                          │
└──────────────────────────────┘
```

### Slot picker + lock
```
Hall › Pick your slot
[◀ Sat 24 Oct ▶]
 4:00 ░░ 4:30 ░░ 5:00 ██ 5:30 ██ 6:00 ██ 6:30 ██ 7:00 ██ 7:30 ██ 8:00 ○
 (free ○ / selected █ / booked ░)
Duration: [2h] [3h ✓] [4h]
Guests: [ 25 ]   Event: [Birthday ▾]
──────────────────────────────
⏳ Held for 09:42 ▓▓▓▓▓▓░░░
Base ₹1,350 · Member −₹135 · Fee ₹60.75 · Tax ₹229.64 = ₹1,505.39
[ Continue to payment ]
```

### QR wallet (Resident)
```
┌──────────────────────────────┐
│ Booking SS-2026-000123       │
│ Green Meadows Hall           │
│ Sat 24 Oct · 5:00–8:00 pm    │
│ ┌────────────┐               │
│ │  ▓▓ QR ▓▓  │  Valid from   │
│ │  ▓▓▓▓▓▓▓▓  │  4:30 pm      │
│ └────────────┘               │
│ 🔒 At the gate you'll get an │
│    OTP on 98••••3210         │
│ [Add decorator] [Directions] │
└──────────────────────────────┘
```

### Owner dashboard (desktop)
```
KPI row:  Bookings 42 | Occupancy 38% | Revenue ₹48,600 | Trust 88
Live status: 🟢 Free now · Next: 5:00 pm Priya S.
[Revenue by week — bar]   [Bookings by event type — donut]
[Usage heatmap Mon..Sun × 7am..11pm]
[Smart Pricing Advisor: Tue 11am–2pm idle 12 wks → suggest −15%  (Apply)]
[Recent bookings table]
```

## 11. Accessibility (WCAG 2.1 AA)

- Colour contrast ≥ 4.5:1 (text) and ≥ 3:1 (UI components); semantic tokens above are pre-checked.
- Full keyboard navigation; visible focus ring (2 px `primary-600` + offset).
- Labels linked to inputs; errors announced (`aria-live="polite"`); form errors also shown in text.
- Calendar/slot grid operable via keyboard and screen reader ("Saturday 5:00 pm, available").
- Touch targets ≥ 44 px (watchman ≥ 64 px). Support 200% text zoom without loss.
- `prefers-reduced-motion` and dark mode respected.
- Alt text on hall photos (owner-supplied captions); decorative images `alt=""`.
- Run **axe** checks in Playwright on all key pages.

## 12. UX Requirements (every screen)

- ✅ Mobile responsive (375 / 768 / 1440)
- ✅ Loading (skeletons), Empty, Error (with retry) states
- ✅ Accessible forms with inline validation
- ✅ Currency ₹ with Indian grouping (₹1,23,456); dates "Sat, 24 Oct 2026", time 12-hour with am/pm in IST
- ✅ Destructive actions confirmed via dialog with consequence text (e.g., refund amount on cancel)
- ✅ Idle-safe: never lose an in-progress booking on refresh (state from server by booking id)

## 13. Microcopy Rules

- Identity: "We verify your identity once so the watchman can confirm it's really you. We never store your Aadhaar number."
- Trust: "Score built only from stays confirmed at the gate."
- Lock: "We're holding this slot for you for 10 minutes."
- Errors: what happened + what to do ("That slot was just taken. Here are 3 nearby times.").
- Avoid jargon (KYC → "Identity check", OTP → "6-digit code").

## 14. Iconography & Imagery

- **lucide-react**, 24 px, 1.75 stroke. Consistent metaphors: `MapPin` distance, `Users` capacity, `Snowflake` AC, `Car` parking, `ShieldCheck` verified, `QrCode`, `Clock`, `Star`.
- Hall photos: 16:9 cards, lazy-loaded, blurred placeholder. Empty photo → neutral illustration with hall initials.

## 15. Do / Don't

| Do | Don't |
|----|-------|
| Reuse `components/` primitives | Create one-off buttons/cards per page |
| Use tokens (`bg-primary-600`) | Hard-code hex values in components |
| Show verification states prominently | Hide identity/QR steps in menus |
| Keep watchman screens minimal | Add tables/filters to watchman flow |
