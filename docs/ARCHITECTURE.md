# Technical Architecture: Event Attendance System

This document outlines the software design, patterns, and component interactions in the **Event Attendance System** Android application.

---

## 1. Architectural Layers

The application is structured following Clean Architecture principles:

```
┌────────────────────────────────────────────────────────┐
│                   Jetpack Compose UI                   │
│   (Screens, Components, Navigation Compose, Material3) │
└───────────────────────────▲────────────────────────────┘
                            │ UI State & Actions
┌───────────────────────────┴────────────────────────────┐
│                    ViewModel Layer                     │
│    (StateFlow, Coroutine Scope, One-Time Navigation)   │
└───────────────────────────▲────────────────────────────┘
                            │ Domain Operations
┌───────────────────────────┴────────────────────────────┐
│                     Domain Use Cases                   │
│  (SearchStudentsUseCase, StudentQrParser, Validator)   │
└───────────────────────────▲────────────────────────────┘
                            │ Data Contracts
┌───────────────────────────┴────────────────────────────┐
│                    Repository Layer                    │
│   (AttendanceRepository, EventRepository, AuthRepo)    │
└───────────────────────────▲────────────────────────────┘
              ┌─────────────┴─────────────┐
              ▼                           ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│      Local Source        │  │      Remote Source       │
│  (Room SQLite + Indices) │  │  (Supabase API + Realtime│
│  (DataStore Preferences) │  │   + Postgres Atomic RPC) │
└──────────────────────────┘  └──────────────────────────┘
```

---

## 2. Core Functional Modules

### 2.1 Fast Local Indexed Search (<50ms)
- **Local Preloading**: When an operator selects an event, the event roster is cached into the Room database (`event_students` table).
- **Composite Indexes**:
  - `Index(value = ["eventId", "enrollmentNo"], unique = true)`
  - `Index(value = ["eventId", "name"])`
- **Keystroke Search**: The query is evaluated locally in-memory and via SQLite without hitting the network on every keystroke.
- **Exact Match Safeguards**:
  - When query matches an enrollment number completely and uniquely (`exactEnrollmentMatches.size == 1`), `autoNavigateStudent` is triggered once.
  - When query matches a student's full name completely and uniquely (`exactNameMatches.size == 1`), `autoNavigateStudent` is triggered once.
  - Partial matches (even if only 1 result remains in the filtered list) **never** trigger auto-navigation.
  - Reset state: Returning from attendance clears the search query and resets the keyboard state for the next attendee.

### 2.2 Student ID QR Code Parser
- **Payload Format**: `[EnrollmentNumber]|[Department]|[Degree]|[AdditionalID]`
  - Example: `12025052002292|CSE|B.TECH|104202505200628`
- **Extraction Rules**:
  - Delimited by `|`.
  - Only **Field 1** is extracted.
  - Surrounding whitespace is trimmed.
  - Leading zeroes are strictly preserved as `String` (never parsed to numeric type).
  - Plain enrollment numbers without delimiters are also supported.
  - Payloads with missing enrollment numbers or delimiters-only are rejected.

### 2.3 Continuous Scanner Engine
- **CameraX + Google ML Kit Barcode Scanning**.
- **CameraX Strategy**: `STRATEGY_KEEP_ONLY_LATEST` on a dedicated background thread for high frame-rate responsiveness.
- **In-flight Mutex**: Drops incoming frames while an attendance write is in progress.
- **Cooldown Re-arm**: 2.5-second cooldown timer for identical barcodes prevents repeated submissions if an ID card remains in frame.
- **Instant Auto-Resume**: On confirmed attendance, emits a subtle tone (`ToneGenerator.TONE_PROP_ACK`), displays the green success banner for 1.0 second, and resumes scanning without requiring any button press.

### 2.4 Streaming Spreadsheet Engine
- **Pure Android Streaming**: Parses `.xlsx` OOXML ZIP containers using Android's native `XmlPullParser` on `xl/sharedStrings.xml` and `xl/worksheets/sheet1.xml`.
- **Zero Third-Party Bloat**: Avoids 40MB Apache POI JARs and duplicate `javax.xml` DEX issues.
- **RFC-4180 CSV Engine**: Handles multi-line cells, escaped quotes, commas, and BOM markers.
- **Flexible Column Mapping**: Name and Enrollment are required; Section, Roll No, Email, and Mobile can be left unmapped.
- **Conflict Resolution**: `Skip Existing` or `Update Details`. Existing attendance records are never overwritten.

### 2.5 Concurrency-Safe Supabase Attendance
- **PostgreSQL Atomic RPC**: `mark_attendance` executes inside a transactional function with unique constraint `(event_id, enrollment_no)`.
- **Race Condition Immunity**: Concurrent scans from multiple operators cannot insert duplicate attendance records. The second transaction triggers a `unique_violation` caught by PostgreSQL and returns `ALREADY_MARKED` with the original timestamp and operator.
- **Realtime Synchronization**: Supabase Realtime channels listen to changes on `attendance_records` and synchronize them into Room across all connected operator devices.
