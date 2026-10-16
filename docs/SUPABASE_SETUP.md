# Supabase Backend Setup & Migration Guide

This guide describes how to configure the Supabase PostgreSQL backend, execute migrations, enforce Row Level Security (RLS), and provision the designated Administrator account.

---

## 1. Create a Supabase Project

1. Log in to [Supabase Console](https://supabase.com).
2. Click **New Project** and configure your organization, project name, database password, and region.
3. Once initialized, navigate to **Project Settings -> API** to retrieve:
   - **Project URL** (e.g., `https://xyzcompany.supabase.co`)
   - **anon / public key** (safe to embed in the mobile client)

---

## 2. Execute SQL Migrations

Open the **SQL Editor** in the Supabase dashboard and execute the migration files in numerical order:

### Step 1: Core Schema & Performance Indexes
Open and run [supabase/migrations/001_initial_schema.sql](file:///d:/Programming%20Project/Event%20Attendence%20system/supabase/migrations/001_initial_schema.sql):
- Creates `app_role` enum (`admin`, `operator`).
- Creates `profiles`, `events`, `students`, `event_students`, `attendance_records`, `import_batches`, and `audit_logs`.
- Adds unique constraint `uq_attendance_event_enrollment UNIQUE (event_id, enrollment_no)` to prevent race conditions.
- Sets up indices for fast sub-50ms lookups.

### Step 2: Row Level Security (RLS)
Open and run [supabase/migrations/002_rls_policies.sql](file:///d:/Programming%20Project/Event%20Attendence%20system/supabase/migrations/002_rls_policies.sql):
- Enables RLS on all public tables.
- Creates helper function `is_admin()`.
- Establishes read/write policies:
  - Operators can view published/open events and record attendance.
  - Only Administrators can create events, manage rosters, and correct attendance records.

### Step 3: Concurrency-Safe Atomic RPC Functions
Open and run [supabase/migrations/003_rpc_functions.sql](file:///d:/Programming%20Project/Event%20Attendence%20system/supabase/migrations/003_rpc_functions.sql):
- Installs `mark_attendance(p_event_id, p_enrollment_no, p_operator_name, p_device_id)`:
  - Validates event status and student roster membership.
  - Atomic insertion with row-level uniqueness constraint.
  - Returns structured JSON status: `SUCCESS`, `ALREADY_MARKED`, `STUDENT_NOT_REGISTERED`, `EVENT_CLOSED`, or `PERMISSION_DENIED`.
- Installs `correct_attendance(p_attendance_id, p_new_status, p_reason)` with automatic append-only logging to `audit_logs`.
- Adds tables to `supabase_realtime` publication for live multi-device synchronization.

### Step 4 (Optional): Seed Sample Event Data
Open and run [supabase/seed.sql](file:///d:/Programming%20Project/Event%20Attendence%20system/supabase/seed.sql):
- Seeds the "Annual Tech Symposium 2026" sample event.
- Registers test students including IDs `12025052002292` and `00123456789012` (verifying leading zeros).

---

## 3. Provisioning the First Administrator

To securely grant Administrator privileges without exposing service-role keys:

1. In Supabase Dashboard, go to **Authentication -> Users** and click **Add User -> Create User**.
2. Enter the admin email (e.g., `admin@college.edu`) and set a strong password.
3. Open the **SQL Editor** and run:
   ```sql
   UPDATE public.profiles
   SET role = 'admin', full_name = 'Lead Administrator'
   WHERE email = 'admin@college.edu';
   ```
4. Verify by checking:
   ```sql
   SELECT id, email, full_name, role FROM public.profiles WHERE role = 'admin';
   ```

---

## 4. Connecting the Android App

### Option A: Via `local.properties` (Build-Time)
Add your credentials to `local.properties`:
```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Option B: In-App Settings (Runtime)
1. Launch the Android application.
2. Navigate to **Settings & Connection** (from Event Selection top-bar).
3. Paste your **Supabase Project URL** and **Anon Key**.
4. Tap **Save Credentials**.
