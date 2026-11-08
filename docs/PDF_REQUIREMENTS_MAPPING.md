# Rupeek Maid Booking System — requirements mapping

The scope here is **Question B, Maid Booking System**, pages 1–4 of the supplied PDF.

| PDF requirement | Implementation | Coverage |
|---|---|---|
| 3.1 Maid discovery by locality/service/date/time | `MaidController`, `MaidSearchServiceImpl` | GET & JSON POST |
| 3.1 Rating, price, skills, gender filters | `MaidSearchRequest`, `strategy/filter` component implementations | Composable strategies |
| 3.1 Only actually available maids | `AvailabilityServiceImpl`, `TimeSlot` | Validates shift windows and existing reservations |
| 3.2 Maid onboarding | `MaidOnboardRequest`, `MaidServiceImpl` | Validates profile, skills, weekly windows |
| 3.3 Instant booking | `InstantBookingPlanner` | Uses injected `Clock` |
| 3.3 Scheduled booking | `ScheduledBookingPlanner` | Future start and duration validation |
| 3.3 Recurring booking | `RecurringBookingPlanner` | Repeated weekdays, bounded occurrences |
| 3.3 Common booking abstraction | `BookingPlanner` + registry | Additional planners are auto-discovered as Spring components |
| 3.3 No overlapping bookings | `MaidRepository.withMaidLock`, `AvailabilityServiceImpl` | Per-maid atomic reserve under process-local lock |
| 3.4 Payment | `PaymentController`, `BookingServiceImpl` | Mock per-occurrence payments |
| 3.4 Pluggable UPI/card/wallet | `PaymentGateway`, `strategy/payment` mock gateway implementations | Interface + method routing |
| 3.5 Cancel individual recurring occurrence | `CancelBookingRequest` scope OCCURRENCE | Releases selected occurrence |
| 3.5 Cancel entire series | `CancelBookingRequest` scope SERIES | Cancels remaining future slots |
| 3.5 Pluggable refund policy | `RefundPolicy`, `TimeBasedRefundPolicy` | Configurable policy implementation |
| REST, validation and errors | Controllers, `dto`, `ApiExceptionHandler` | JSON API, HTTP 400/404/409 |
| In-memory persistence behind interfaces | `repository` package | No external DB required |
| Mock external dependencies | `strategy/payment` mock gateway implementations | CARD/UPI/WALLET mocked; no real charging/refunds |
| Unit and integration tests | `BookingServiceTest`, `BookingApiIntegrationTest` | Core service tests and MockMvc API tests |
| Bonus: simultaneous booking concurrency | `BookingServiceTest.concurrentBookingAttemptsCanOnlyReserveOnce` | 16 contenders, exactly one success |
| Bonus: pluggable pricing | `PricingPolicy`, `HourlyPricingPolicy` | Hourly rate prorated to minutes |
| Bonus: payment idempotency | `BookingServiceImpl.pay` | Per-occurrence replay key |

## Not implemented because PDF says out of scope

Authentication and authorization; UI/frontend; production persistence and migrations; deployment/CI/CD; real payment gateway; exhaustive features. Swagger/OpenAPI is optional bonus and was not prioritized over the core working features.

## Explicit design assumptions

- Time zone: Asia/Kolkata, using local dates/times for demonstration.
- Bookings are confined to a single calendar day, 15–480 minutes.
- Recurrence expands into concrete occurrences for up to 3 months (maximum 100).
- Reservations are half-open intervals `[start, end)`.
- Cancellation refund rate: 100% (24+ hours before start), 50% (1–24h), 0% (<1h).
- The booking is initially reserved; payment is a separate operation per occurrence.
- Single-process semantics: in-memory data and locks do not survive a restart or coordinate across replicas.

## Follow-up discussion topics

Explain why you chose one `Booking` with many `BookingOccurrence`s, how the maid lock prevents two overlapping reservations, why payment is per occurrence, how you add another booking or payment method, and what would change if a relational database replaced the in-memory repository.

## Requested code-quality conventions

- Standard Java classes with Lombok `@Data` for generated accessors and `@Builder` on validated domain constructors; no `record` declarations. Domain validation and controlled state changes remain explicit.
- DTOs grouped by `maid`, `booking`, `payment` and `common`, with Lombok `@Data`, `@Builder`, constructor annotations and Jackson `@JsonNaming` for snake_case JSON.
- Strategy interface and its implementations colocated in each feature package.
- No manual `@Bean` configuration of repositories or services. Spring stereotypes plus constructor injection handle wiring.
- POST endpoints use `@Valid @RequestBody` JSON DTOs; GET endpoints use conventional path/query parameters.

- Spring constructor injection via `@RequiredArgsConstructor(onConstructor_ = @Autowired)` or explicit `@Autowired` constructors when logic is needed.
- Domain and response DTO mapping uses Lombok `@Builder`; `ResponseEntity` provides HTTP 201 and `Location` for creation endpoints.
