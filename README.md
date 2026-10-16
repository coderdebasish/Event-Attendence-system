# Event Attendance System

A production-quality native Android application designed for high-speed, reliable student attendance marking during college events, hackathons, workshops, and competitions. Backed by **Supabase PostgreSQL** with real-time synchronization, indexed local Room search, continuous CameraX/ML Kit QR scanning, flexible Excel/CSV roster import with column mapping, and role-based security.

---

## Key Features

1. **Unified Attendance Workspace**:
   - Single-screen workflow supporting enrollment search, name search, and continuous camera scanning without separate screens.
   - Preserves selected event context throughout the session.
2. **Sub-50ms Indexed Local Search**:
   - Preloads event rosters into Room SQLite with composite indices on `(event_id, enrollment_no)` and `(event_id, name)`.
   - Zero network round-trips while typing.
   - **Exact Unique Match Auto-Navigation**: Typing a full unique enrollment number (e.g. `2292`) or full unique student name immediately opens the student's profile without an extra tap.
   - **Exact-Match Safeguards**: Partial prefixes never trigger false auto-navigation.
3. **Student ID QR Parser (Field 1 Extraction)**:
   - Delimited payload format: `[EnrollmentNumber]|[Department]|[Degree]|[AdditionalID]`
   - Strictly extracts **Field 1** as text, preserving leading zeroes (e.g. `00123456789012`).
   - Plain enrollment numbers without delimiters are also supported.
4. **Continuous CameraX & ML Kit Scanner**:
   - In-flight mutex locks and 2.5s duplicate scan cooldown prevent repeated submissions of the same code.
   - Confirmation audio tone via `ToneGenerator`.
   - Dynamic 1.0-second success overlay with automatic non-blocking resume loop.
5. **Excel/CSV Ingestion with Flexible Column Mapping**:
   - High-performance streaming parser for `.xlsx`, `.xls`, and `.csv` files.
   - Name and Enrollment are required; Section, Roll No, Email, and Mobile can be left unmapped.
   - In-file duplicate detection, roster conflict resolution (`Skip Existing` vs `Update Details`).
   - Export attendance reports to CSV and Excel.
6. **Supabase PostgreSQL & Security**:
   - PostgreSQL Row Level Security (RLS) on all exposed tables.
   - Concurrency-safe atomic `mark_attendance` stored procedure preventing duplicate scans and race conditions across multiple operator devices.
   - Real-time updates via Supabase Realtime channels.
   - Audit trail logging all administrative attendance corrections.

---

## Tech Stack

- **Platform**: Android (minSdk 26, targetSdk 35, compileSdk 35)
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose + Material 3 + Navigation Compose
- **Local Database**: Room 2.6.1 SQLite
- **Camera & Scanning**: CameraX 1.4.1 + Google ML Kit Barcode Scanning 17.3.0
- **Preferences**: Android DataStore Preferences
- **Dependency Injection**: Hilt 2.51.1 + KSP 2.0.21
- **Backend**: Supabase Kotlin SDK (Postgrest, Auth, Realtime) + PostgreSQL
- **Build System**: Gradle 8.14 (Kotlin DSL)

---

## Project Structure

```
├── app/
│   ├── src/main/
│   │   ├── java/com/eventattendance/app/
│   │   │   ├── EventAttendanceApp.kt
│   │   │   ├── MainActivity.kt
│   │   │   ├── core/
│   │   │   │   ├── designsystem/     # Theme, Colors, Typography, Reusable Components
│   │   │   │   ├── audio/            # Low-latency ToneGenerator & Haptics
│   │   │   │   ├── config/           # Supabase Configuration Validator
│   │   │   │   └── util/             # StudentQrParser, Result wrappers
│   │   │   ├── data/
│   │   │   │   ├── local/            # Room Database, DAOs, UserPreferences DataStore
│   │   │   │   ├── model/            # Student, Event, AttendanceRecord, Outcome, Batch
│   │   │   │   ├── remote/           # SupabaseClientProvider, RealtimeManager, API
│   │   │   │   ├── repository/       # Attendance, Event, and Auth Repositories
│   │   │   │   └── excel/            # Streaming XLSX & RFC-4180 CSV Parser, Exporter
│   │   │   ├── domain/usecase/       # SearchStudentsUseCase with exact match safeguards
│   │   │   ├── feature/
│   │   │   │   ├── splash/           # Splash screen & routing
│   │   │   │   ├── onboarding/       # Operator onboarding & display name
│   │   │   │   ├── auth/             # Administrator login
│   │   │   │   ├── events/           # Event selection & creation
│   │   │   │   ├── workspace/        # Unified attendance workspace & keystroke search
│   │   │   │   ├── student/          # Minimal 6-field student profile
│   │   │   │   ├── scanner/          # Continuous CameraX + ML Kit Scanner
│   │   │   │   ├── admin/            # Admin dashboard, roster, live monitoring, audit
│   │   │   │   ├── importexport/     # Spreadsheet upload, mapping, validation
│   │   │   │   └── settings/         # Connection credentials & operator settings
│   │   │   ├── navigation/           # Compose NavHost & route definitions
│   │   │   └── di/                   # Hilt Dependency Injection module
│   │   └── res/                      # Drawables, mipmaps, strings, themes, file_paths
│   └── src/test/                     # Comprehensive Unit Tests
├── supabase/
│   ├── migrations/
│   │   ├── 001_initial_schema.sql    # Schema, tables, constraints, indices
│   │   ├── 002_rls_policies.sql      # Row Level Security policies
│   │   └── 003_rpc_functions.sql     # Atomic mark_attendance & correction RPCs
│   └── seed.sql                      # Sample event & test student records
├── docs/
│   ├── ARCHITECTURE.md               # Detailed system design
│   ├── SUPABASE_SETUP.md             # Database setup & first admin provisioning
│   └── USER_GUIDE.md                 # End-to-end operator & admin instructions
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## Build & Test Instructions

### 1. Prerequisites
- **Java**: JDK 21 (e.g., Android Studio JBR / Eclipse Temurin 21)
- **Android SDK**: API 35 SDK platforms & build-tools (35.0.0+)
- **Environment variables**:
  - `JAVA_HOME` pointing to JDK 21
  - `ANDROID_HOME` pointing to your Android SDK

### 2. Execute Automated Unit Tests
```bash
./gradlew test
```
Runs unit test suites covering:
- Student ID QR code pipe-separated parsing and leading zero preservation
- Exact vs. partial keystroke search behavior and navigation safeguards
- Excel and CSV column mapping, validation, and in-file duplicate detection
- Event status and attendance state management

### 3. Assemble Debug APK
```bash
./gradlew assembleDebug
```
The compiled APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Supabase Quickstart

1. Create a project at [supabase.com](https://supabase.com).
2. Execute the migration scripts in the Supabase SQL Editor:
   - `supabase/migrations/001_initial_schema.sql`
   - `supabase/migrations/002_rls_policies.sql`
   - `supabase/migrations/003_rpc_functions.sql`
   - `supabase/seed.sql` (optional sample data)
3. Provision your first administrator:
   ```sql
   UPDATE public.profiles
   SET role = 'admin', full_name = 'Admin Name'
   WHERE email = 'your-admin@email.com';
   ```
4. Enter your project URL and publishable anon key in the in-app **Settings & Connection** screen.
