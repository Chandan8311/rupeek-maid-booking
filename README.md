# Maid Booking System

A runnable **Java 17 + Spring Boot 3** backend for onboarding home-service professionals, discovering available maids, making instant/scheduled/recurring reservations, collecting mock payments, and cancelling/refunding bookings.


## Quick start

**Prerequisites:** JDK 17+ and Maven 3.9+.

```bash
java -version
mvn -version
mvn clean test
mvn spring-boot:run
```

Base URL: `http://localhost:8080`. No database server, environment variables, payment-gateway account or Docker installation is required. In-memory data resets when the process restarts.

**IntelliJ:** Open the directory containing `pom.xml` as a Maven project; choose JDK 17+; run `MaidBookingApplication` or `mvn spring-boot:run`.

## Domain and relationships

```text
Maid
  id, locality, gender, rating, skills, hourlyRate
  availability: weekly day-of-week + start/end windows

Booking
  id, maidId, customerId, serviceType, type, createdAt
     |
     +-- one or more BookingOccurrence
           id, maidId, slot, price, RESERVED/CANCELLED
           |
           +-- optional PaymentReceipt
                 method, amount, refundedAmount,
                 providerPaymentId, idempotencyKey
```

## API contract: POST always uses JSON request bodies

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/v1/maids` | Onboard maid |
| `GET` | `/api/v1/maids/{maidId}` | View profile |
| `GET` | `/api/v1/maids?startAt=...&durationMinutes=...` | Search by query parameters |
| `POST` | `/api/v1/bookings` | Instant/scheduled/recurring booking |
| `GET` | `/api/v1/bookings/{bookingId}` | Retrieve booking + occurrences |
| `POST` | `/api/v1/payments` | Pay for one occurrence |
| `POST` | `/api/v1/bookings/cancellations` | Cancel occurrence or full series |

### 1. Onboard maid

```http
POST /api/v1/maids
Content-Type: application/json
```

```json
{
  "name": "Anita",
  "locality": "HSR Layout",
  "gender": "FEMALE",
  "rating": 4.8,
  "hourly_rate": 300,
  "skills": ["cleaning", "cooking"],
  "availability": [
    {"day_of_week": "MONDAY", "from": "08:00:00", "to": "20:00:00"},
    {"day_of_week": "WEDNESDAY", "from": "08:00:00", "to": "20:00:00"}
  ]
}
```

**Response:** HTTP 201, JSON maid profile with a generated `id` and `Location` header. Overlapping windows on the same weekday are rejected.

### 2. Search maids 

```http
GET /api/v1/maids/
Content-Type: application/json
```


The result includes **only maids available for the entire requested interval**, filtered by composable `MaidFilter` strategies. The `startAt` and `durationMinutes` fields are mandatory; all other filters are optional.

GET alternative:

```http
GET /api/v1/maids?serviceType=cleaning&startAt=2030-01-07T10:00:00&durationMinutes=60
```

### 3. Create scheduled booking

```http
POST /api/v1/bookings
Content-Type: application/json
```

```json
{
  "maid_id": "REPLACE_WITH_MAID_UUID",
  "customer_id": "CUSTOMER-101",
  "service_type": "cleaning",
  "type": "SCHEDULED",
  "start_at": "2030-01-07T10:00:00",
  "duration_minutes": 60
}
```

The response is HTTP 201 with `id`, `maidId`, `type`, and a list of occurrences with occurrence ID, slot, price and `RESERVED` status.

To book **INSTANT**, set `type` to `INSTANT` and omit `startAt`; the injected clock determines the current time. The maid must be on duty at that moment.

To book **RECURRING**, set `type` to `RECURRING` and supply `startAt`, `repeatOn` (weekday enum array), and `repeatUntil` (inclusive date):

```json
{
  "maid_id": "REPLACE_WITH_MAID_UUID",
  "customer_id": "CUSTOMER-101",
  "service_type": "cleaning",
  "type": "RECURRING",
  "start_at": "2030-01-07T10:00:00",
  "duration_minutes": 60,
  "repeat_on": ["MONDAY", "WEDNESDAY"],
  "repeat_until": "2030-01-31"
}
```

A recurring booking creates one occurrence per matching weekday, up to 100 occurrences within a three-month horizon. **All-or-nothing:** if any occurrence is unavailable, the entire request is rejected without reserving partial inventory.

### 4. Pay for a booking occurrence

```http
POST /api/v1/payments
Content-Type: application/json
```

```json
{
  "booking_id": "REPLACE_WITH_BOOKING_UUID",
  "occurrence_id": "REPLACE_WITH_OCCURRENCE_UUID",
  "method": "UPI",
  "idempotency_key": "customer101-occ1-pay1"
}
```

Available mock payment methods: `CARD`, `UPI`, `WALLET`. Each implements the common `PaymentGateway` contract. For the same occurrence, repeating the same idempotency key and method returns the existing payment receipt; attempting an additional payment with a different key or method returns HTTP 409.

### 5. Cancel one occurrence

```http
POST /api/v1/bookings/cancellations
Content-Type: application/json
```

```json
{
  "booking_id": "REPLACE_WITH_BOOKING_UUID",
  "scope": "OCCURRENCE",
  "occurrence_id": "REPLACE_WITH_OCCURRENCE_UUID"
}
```

### 6. Cancel the rest of a series

Same endpoint, different body:

```json
{
  "booking_id": "REPLACE_WITH_BOOKING_UUID",
  "scope": "SERIES"
}
```

The response includes `cancelled_occurrence_ids` and `total_refund` (snake_case JSON). `SERIES` cancels remaining future RESERVED occurrences, skips previously cancelled/elapsed ones, and returns 409 if no future reservation remains. Cancelled inventory becomes available for new bookings.

## Availability and cancellation rules

- Reservations use half-open intervals `[start, end)`: 10:00–11:00 conflicts with 10:30–11:30 but **not** with 11:00–12:00.
- Requested duration: **15–480 minutes**; cross-midnight bookings are intentionally not supported.
- The full slot must fit within one weekly availability window for that maid.
- A maid cannot have two overlapping active (`RESERVED`) occurrences.
- Cancellation is allowed only before an occurrence's start.
- Illustrative refund policy for a **paid** future occurrence: **100%** if at least 24 hours ahead, **50%** if between 1 and 24 hours, **0%** if less than an hour ahead. Unpaid cancellations refund zero. The policy is behind `RefundPolicy`, not hard-coded into the controller.
- Currency is treated as a generic amount with two-decimal precision; no real payment provider is contacted.
- All `LocalDateTime` fields are interpreted as **Asia/Kolkata** wall-clock times in this exercise; production should use time-zone-aware instants.

## Design patterns and extensibility

| Requirement | Abstraction | How to extend |
|---|---|---|
| Instant/scheduled/recurring | `BookingPlanner` | Implement another `@Component` planner under `strategy/booking` |
| Filters by skill, locality, rating, etc. | `MaidFilter` | Add a `@Component` class under `strategy/filter` |
| Payment by UPI, card, wallet | `PaymentGateway` | Add a `@Component` payment implementation under `strategy/payment` |
| Hourly / dynamic pricing | `PricingPolicy` | Provide an alternative `PricingPolicy` implementation (choose via configuration/`@Primary` when multiple are present) |
| Time-based / custom refunds | `RefundPolicy` | Provide a new `RefundPolicy` implementation (choose via configuration/`@Primary` when multiple are present) |
| In-memory vs database | `MaidRepository`, `BookingRepository` | Supply SQL/JPA implementations without changing business services |
| API vs business model | DTOs and `mapper/` | Change REST payloads separately from domain classes |

Spring component scanning and single-constructor injection wire the services and repositories automatically. `config/AppConfiguration.java` defines only the `Clock` bean. Controllers handle request validation and HTTP mapping; domain logic remains in services and strategies.

## Concurrency and correctness

**Double-booking:** `InMemoryMaidRepository` provides one `ReentrantLock` per maid. Booking creation holds this lock around **check availability → price → save all occurrences**. Different maids can be booked concurrently. This is correct for the assignment's single-process storage model, **not a distributed locking guarantee**.

**Payment / cancellation:** Each `BookingOccurrence` has synchronized transitions for payment and cancellation, with documented lock ordering: cancellation acquires maid lock before occurrence monitor. A duplicate payment with the same key does not re-charge the mock provider.

**Inventory release:** Cancellation marks an occurrence `CANCELLED`; discovery ignores cancelled occurrences when checking conflicts. The historical booking is retained.

**Atomic recurring reservation:** Before saving any occurrences, the service validates **all planned slots** under the maid lock. A conflict leaves inventory unchanged.

## Tests

```bash
mvn clean test
```

The standard JUnit 5 suite includes:

- Instant, scheduled, and recurring booking logic
- Hourly pricing, adjacency vs overlap, availability windows
- Maid discovery and composable filtering
- Recurring all-or-nothing conflicts, per-occurrence payment
- Cancellation and refund policy boundaries
- Payment idempotency and invalid ownership
- **16 parallel attempts for the same maid/time slot: exactly one succeeds**
- Spring `MockMvc` request validation and end-to-end REST flows using JSON bodies

See `VERIFICATION.md` for checks actually executed in the authoring environment and `docs/PDF_REQUIREMENTS_MAPPING.md` for a section-by-section mapping to the PDF.

## Assumptions / out of scope (per case-study brief)

- No login, RBAC, UI, Docker, deployment pipeline, cloud infrastructure, real payment gateways or production-grade database.
- Storage is intentionally in memory; state is lost on process restart. Repository contracts make replacement possible.
- No cross-day availability, travel buffers, holiday calendars or infinite recurrence.
- No actual payment provider callbacks or distributed transaction logic.
- Basic price is hourly rate prorated to minutes (two-decimal rounding).
- Business constants and refund policy are exercise assumptions, not Rupeek policy.
