# NV-1 Security Acceptance Report: Dual-Layer Authentication

This report documents the security posture of the QR + OTP dual-layer authentication mechanism for hall entry (Watchman App), verifying that it effectively stops common impersonation vectors compared to a QR-only baseline.

## Overview
SmartSpace employs a dual-layer verification system for entry:
1. **Layer 1 (What you have):** A time-bound, cryptographically signed Ed25519 QR token.
2. **Layer 2 (What you can receive):** A dynamic OTP sent to the booker's registered mobile number/email upon a valid scan.

## Scenarios Tested

### 1. QR Screenshot Sharing
- **Attack Vector:** The legitimate booker screenshots their QR code and sends it to an unauthorized friend via WhatsApp to gain entry.
- **Expected Outcome (QR-only):** The friend gains entry.
- **Outcome with SmartSpace (QR + OTP):** **STOP AT OTP.**
- **Details:** The watchman scans the QR code. The cryptographical signature is valid, so the system issues a **HOLD** verdict. An OTP is instantly dispatched to the *legitimate booker's* registered phone number. The friend at the gate does not receive the OTP. After 3 minutes or 3 failed guesses, the attempt is locked out. Access denied.

### 2. Forwarded QR via Email
- **Attack Vector:** An attacker intercepts or is forwarded the booking confirmation email containing the QR code.
- **Expected Outcome (QR-only):** Attacker gains entry.
- **Outcome with SmartSpace (QR + OTP):** **STOP AT OTP.**
- **Details:** Similar to the screenshot attack, the attacker possesses the valid QR but not the registered phone. The watchman issues a HOLD. The OTP goes to the legitimate user. Access denied.

### 3. Expired QR
- **Attack Vector:** A user tries to use a QR code from a booking that ended yesterday.
- **Outcome with SmartSpace:** **REJECTED INSTANTLY.**
- **Details:** The QR payload contains a `window_end` timestamp. Before even checking the database, the backend (and offline verifier) checks `current_time < window_end`. It fails. The watchman app immediately shows a **STOP (Expired)** verdict.

### 4. Wrong Hall
- **Attack Vector:** A user with a valid booking for "Hall A" tries to use their QR code to enter "Hall B".
- **Outcome with SmartSpace:** **REJECTED INSTANTLY.**
- **Details:** The QR token contains the `hall_id`. The watchman's scanner session is scoped to their assigned hall. The `POST /api/entry/scan` endpoint verifies that `token.hall_id == watchman.assigned_hall_id`. It fails, returning **STOP (Wrong Hall)**.

### 5. OTP Guessing / Brute Force
- **Attack Vector:** An attacker with a valid QR code reaches the HOLD state and attempts to guess the 6-digit OTP (1 in 1,000,000 chance).
- **Outcome with SmartSpace:** **RATE LIMITED & LOCKED OUT.**
- **Details:** The `OtpService` enforces a strict 3-attempt limit. On the 3rd failed attempt, the gate session is invalidated, and the user is locked out. Further, the OTP itself expires in 180 seconds.

## Conclusion
The dual-layer mechanism successfully mitigates impersonation, screenshot-sharing, and brute-force attacks, providing significantly higher assurance of identity than standard QR ticketing systems.
