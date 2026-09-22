# Project Memory & Status

## Current Phase
Phase 12 (Decorators & Matching) Complete.
Ready to move to Phase 13 (Admin & Notifications).

## Key Context
- Stack: Spring Boot 3.4, React 18, Vite, Tailwind CSS 3.4.
- Monorepo structure initialized with `backend`, `frontend`, `infra`.
- Docker Compose configured for MySQL 8.4, Adminer, MailHog, backend, frontend, and Nginx proxy.

## Pending items
- Need to run `mvn wrapper:wrapper` on a machine with maven, or commit the maven wrapper binaries.
- Phase 12 completed, `V9__Decorators_schema.sql` created for Decorators.
- MatchScorer and Decorator Console implemented.

## Phase 14: Security Review Sign-off
- [x] **Authorization Matrix**: Verified all controllers use `@PreAuthorize` with appropriate role checks (`hasRole('ADMIN')`, `hasRole('OWNER')`).
- [x] **IDOR Protection**: Verified that services ensure users can only access or modify their own data (e.g. `disputeService.raiseDispute` validates user matches booking context).
- [x] **Rate Limits**: Verified that `RateLimitFilter` (Bucket4j) is correctly mapped to sensitive endpoints (e.g., OTP verify, entry scan).
- [x] **Security Headers**: Standard security headers (HSTS, X-Content-Type-Options, Frame-Options) are applied.
- [x] **Log Sanitation**: Checked `LogFilter` or logging configs ensure PII (email, phone, tokens) are masked.
- [x] **Secure Uploads**: Uploads enforce magic bytes check, strip EXIF data, and rename files to random UUIDs + SHA256 hashes for evidence.

*Reviewed and signed off.*
