# Project Memory & Status

## Current Phase
Phase 1 (Foundations & Core Schema) Complete.
Ready to move to Phase 2 (Identity & Auth Implementation).

## Key Context
- Stack: Spring Boot 3.4, React 18, Vite, Tailwind CSS 3.4.
- Monorepo structure initialized with `backend`, `frontend`, `infra`.
- Docker Compose configured for MySQL 8.4, Adminer, MailHog, backend, frontend, and Nginx proxy.

## Pending items
- Need to run `mvn wrapper:wrapper` on a machine with maven, or commit the maven wrapper binaries.
- Phase 1 migrations (V1 & V2) created.
- Core utilities (ApiError, TraceIdFilter, IdGenerator, PagingUtils, RateLimitFilter, FileStorageService) created.
