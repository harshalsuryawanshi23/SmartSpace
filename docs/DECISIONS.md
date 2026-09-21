# Architectural Decision Records (ADRs)

## ADR-001: Tech Stack Selection
**Status**: Accepted
**Context**: Need a robust, scalable stack for local hall booking platform.
**Decision**: Use Spring Boot 3 with Java 21 for backend, React 18 with TypeScript and Tailwind CSS 3.4 for frontend. MySQL 8.4 for relational data.
**Consequences**: Strong typing across the stack, modular monolith structure enables easy microservice extraction later if needed.

## ADR-002: Clock Bean Usage
**Status**: Accepted
**Context**: We need to easily test time-sensitive logic (e.g., booking slot expiration, OTP expiry).
**Decision**: Ban the use of `Instant.now()`, `LocalDateTime.now()`, etc. All time operations must use a injected `java.time.Clock` bean.
**Consequences**: Requires constructor injection of `Clock` in all time-sensitive services. Enforced via ArchUnit tests.
