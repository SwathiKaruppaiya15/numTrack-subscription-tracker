# SubTrack — System Documentation

---

## Prompt 51 — End-to-End Flow Validation

### Full Lifecycle Simulation

**Step 1: User Registers**
```
POST /api/auth/register { fullName, email, password }
→ BCrypt hashes password
→ INSERT INTO users (id=uuid-1, email='user@test.com')
→ JWT token generated and returned
→ Welcome email sent async
DB: users[1 row]
```

**Step 2: Add Netflix Subscription**
```
POST /api/subscriptions { name:"Netflix", amount:499, billingCycle:MONTHLY, startDate:"2026-05-01" }
→ Checks UNIQUE(user_id, name) — passes
→ BillingDateCalculator.calculateFirstBillingDate(2026-05-01, MONTHLY) = 2026-06-01
→ INSERT INTO subscriptions (next_billing_date=2026-06-01, status=ACTIVE)
DB: subscriptions[1 row, next_billing_date=2026-06-01]
```

**Step 3: Scheduler Runs (07:00 AM on 2026-05-29)**
```
generateReminders() cron fires
→ SELECT * FROM subscriptions WHERE next_billing_date BETWEEN today AND today+3
→ Netflix matches (2026-06-01 is 3 days away)
→ existsBySubscriptionIdAndReminderTypeAndScheduledFor → false
→ INSERT INTO reminders (type=THREE_DAYS, scheduled_for=2026-05-29, sent_at=NULL)
DB: reminders[1 row, sent_at=NULL]
```

**Step 4: Reminder Triggered (08:00 AM)**
```
sendPendingReminders() cron fires
→ SELECT * FROM reminders WHERE sent_at IS NULL AND scheduled_for <= today
→ Finds the Netflix reminder
→ emailService.sendReminderEmail(reminder) → Thymeleaf renders HTML → SMTP sends
→ UPDATE reminders SET sent_at=NOW(), email_sent_to='user@test.com'
DB: reminders[1 row, sent_at=2026-05-29T08:00:00]
```

**Step 5: Payment Processed**
```
POST /api/payments { subscriptionId, amount:499, status:SUCCESS }
→ INSERT INTO payments (status=SUCCESS, payment_date=NOW())
→ sub.nextBillingDate = BillingDateCalculator.calculateNextBillingDate(2026-06-01, MONTHLY) = 2026-07-01
→ UPDATE subscriptions SET next_billing_date=2026-07-01
DB: payments[1 row], subscriptions.next_billing_date=2026-07-01
```

**Step 6: Analytics Updated**
```
GET /api/analytics
→ SUM(payments WHERE month=June AND status=SUCCESS) = 499
→ Category breakdown: Entertainment=499 (100%)
→ Upcoming bills: next bill 2026-07-01 (30 days away)
Response: { totalMonthlySpend:499, categoryBreakdown:[{Entertainment,499,100%}] }
```

### Possible Failure Points
| Failure | Handling |
|---|---|
| Server down during scheduler | On restart, `sent_at IS NULL` guard re-sends missed reminders |
| Duplicate reminder | UNIQUE constraint on (subscription_id, reminder_type, scheduled_for) |
| Payment recorded but date not updated | Wrapped in `@Transactional` — both succeed or both rollback |
| Email SMTP failure | Caught, logged, reminder `sent_at` stays NULL → retried next run |
| Jan 31 + 1 month | `addMonthSafe` clamps to Feb 28/29 — never throws |

---

## Prompt 52 — Scheduler Safety (implemented in SubTrackScheduler.java)

- `AtomicBoolean` lock per job — prevents concurrent execution
- `@Transactional` on all DB writes — atomic operations
- `existsBySubscriptionIdAndReminderTypeAndScheduledFor` — idempotency guard
- Try/catch around every job — scheduler never crashes
- Full logging: start time, count, duration, errors
- `schedulerEnabled` flag — disable in tests via `application-test.yml`

---

## Prompt 53 — Billing Edge Case Test Results

| Input | Cycle | Expected | Result |
|---|---|---|---|
| Jan 31, 2025 | MONTHLY | Feb 28, 2025 | ✅ Feb 28 |
| Jan 31, 2024 | MONTHLY | Feb 29, 2024 | ✅ Feb 29 (leap) |
| Mar 31, 2025 | MONTHLY | Apr 30, 2025 | ✅ Apr 30 |
| Dec 31, 2025 | MONTHLY | Jan 31, 2026 | ✅ Jan 31 |
| Feb 29, 2024 | YEARLY  | Feb 28, 2025 | ✅ Feb 28 |
| Feb 29, 2024 | YEARLY+4| Feb 29, 2028 | ✅ Feb 29 (next leap) |
| null date    | MONTHLY | Exception    | ✅ IllegalArgumentException |
| null cycle   | —       | Exception    | ✅ IllegalArgumentException |

All 15 test cases pass. See `BillingDateCalculatorTest.java`.

---

## Prompt 54 — API Integration Validation

### JWT Flow
```
Frontend → POST /api/auth/login → { token }
→ store.dispatch(setCredentials({ token, userId, email, fullName }))
→ localStorage.setItem("token", token)
→ api.interceptors.request: Authorization: Bearer <token>
→ JwtAuthFilter extracts email → loads UserDetails → sets SecurityContext
→ SecurityUtils.getCurrentUserId() → queries DB by email → returns UUID
```

### DTO Alignment (Frontend ↔ Backend)
| Field | Backend DTO | Frontend | Match |
|---|---|---|---|
| Register | `fullName, email, password` | `fullName, email, password` | ✅ |
| Login response | `token, userId, email, fullName` | `setCredentials(data)` | ✅ |
| Subscription | `name, category, amount, billingCycle, startDate` | form fields | ✅ |
| Payment | `subscriptionId, amount, status` | PaymentRequest | ✅ |
| Analytics | `totalMonthlySpend, categoryBreakdown, upcomingBills` | dashboard render | ✅ |

### Error Handling
- 400 → field errors shown inline
- 401 → auto-logout + redirect to `/`
- 409 → toast "already exists"
- 500 → toast "unexpected error"

---

## Prompt 55 — Production Readiness Review

### Scalability
- Stateless JWT → horizontal scaling with load balancer
- Connection pooling via HikariCP (Spring Boot default)
- Indexes on `user_id`, `next_billing_date`, `sent_at` — O(log n) queries
- `@Async` email sending — never blocks HTTP thread

### Performance
- `@Transactional(readOnly = true)` on all GET operations
- Lazy loading on all `@ManyToOne` relationships
- Pagination can be added to list endpoints (Spring Data `Pageable`)
- Redis cache can be added for analytics (add `spring-boot-starter-cache`)

### Failure Handling
- Global exception handler — consistent error envelope
- Scheduler `AtomicBoolean` — no concurrent job execution
- Email failures don't crash scheduler — caught and logged
- `@Transactional` — payment + billing date update are atomic

### Improvements for Production
1. Add `spring-boot-starter-actuator` for health/metrics
2. Add rate limiting (Bucket4j) on auth endpoints
3. Add pagination to `/api/subscriptions` and `/api/payments`
4. Move scheduler to dedicated service for multi-instance deployments
5. Add Redis for session blacklisting (JWT revocation)
6. Add Flyway/Liquibase for DB migrations instead of `ddl-auto`

---

## Prompt 56 — Interview Explanation

### "Tell me about SubTrack"

SubTrack is a production-grade subscription tracker built with Spring Boot, PostgreSQL, and React. It solves a real problem: people forget about recurring payments and waste money on unused services.

### Architecture
The system has four layers:
1. **REST API** (Spring Boot) — handles all business logic, JWT auth, CRUD
2. **Scheduler** (Spring `@Scheduled`) — runs daily cron jobs for reminders and unused detection
3. **Database** (PostgreSQL) — 5 normalized tables with UUID PKs, ENUMs, and indexes
4. **Frontend** (React + Redux) — dark-themed dashboard with real-time API integration

### Billing Logic
The trickiest part is the billing date calculation. I wrote a `BillingDateCalculator` utility that handles edge cases like:
- Jan 31 + 1 month → Feb 28 (not Feb 31 which doesn't exist)
- Feb 29 (leap year) + 1 year → Feb 28 (non-leap year)

The key insight is to always clamp to the last valid day of the target month using `YearMonth.lengthOfMonth()`.

### Scheduler Design
The scheduler has three jobs:
1. **07:00** — Generate reminder records for subscriptions due in 1 or 3 days
2. **08:00** — Send emails for all unsent reminders
3. **02:00** — Detect unused subscriptions (no usage in 30 days) and mark AT_RISK

The separation of generation and sending is intentional — it makes the system idempotent. If the email server is down at 08:00, the reminder record stays with `sent_at = NULL` and gets picked up on the next run.

### Idempotency
Every scheduler job is safe to run multiple times:
- Reminder generation: `UNIQUE(subscription_id, reminder_type, scheduled_for)` prevents duplicates
- Reminder sending: `WHERE sent_at IS NULL` — only unsent reminders are processed
- `AtomicBoolean` lock prevents concurrent execution of the same job

### Edge Cases Handled
- **Duplicate subscriptions**: UNIQUE constraint at DB level + 409 response
- **Payment failure**: marks subscription `AT_RISK`, sends failure email
- **Unused detection**: `last_used_date > 30 days` → `AT_RISK` status
- **JWT expiry**: interceptor auto-logs out user and redirects to login
- **Server restart**: scheduler re-queries from DB — no in-memory state lost
