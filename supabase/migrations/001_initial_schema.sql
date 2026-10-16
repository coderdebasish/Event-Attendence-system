-- ============================================================================
-- Event Attendance System - 001_initial_schema.sql
-- Core schema, constraints, enums, tables, and performance indexes
-- ============================================================================

-- Roles enum
CREATE TYPE app_role AS ENUM ('admin', 'operator');

-- 1. Profiles Table (linked to Supabase auth.users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT,
    full_name TEXT NOT NULL,
    role app_role NOT NULL DEFAULT 'operator',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Trigger to auto-create profile on user signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, email, full_name, role)
    VALUES (
        new.id,
        new.email,
        COALESCE(new.raw_user_meta_data->>'full_name', split_part(new.email, '@', 1), 'Operator'),
        COALESCE((new.raw_user_meta_data->>'role')::app_role, 'operator')
    );
    RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- 2. Events Table
CREATE TABLE IF NOT EXISTS public.events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    event_date TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    status TEXT NOT NULL DEFAULT 'open' CHECK (status IN ('draft', 'published', 'open', 'closed', 'archived')),
    allow_attendance BOOLEAN NOT NULL DEFAULT true,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 3. Students Directory Table (Master Student List)
CREATE TABLE IF NOT EXISTS public.students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_no TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    section TEXT,
    roll_no TEXT,
    email TEXT,
    mobile TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 4. Event Students Table (Event Roster)
CREATE TABLE IF NOT EXISTS public.event_students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES public.events(id) ON DELETE CASCADE,
    student_id UUID REFERENCES public.students(id) ON DELETE SET NULL,
    enrollment_no TEXT NOT NULL,
    name TEXT NOT NULL,
    section TEXT,
    roll_no TEXT,
    email TEXT,
    mobile TEXT,
    status TEXT NOT NULL DEFAULT 'registered',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    CONSTRAINT uq_event_student_enrollment UNIQUE (event_id, enrollment_no)
);

-- 5. Attendance Records Table
CREATE TABLE IF NOT EXISTS public.attendance_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES public.events(id) ON DELETE CASCADE,
    event_student_id UUID REFERENCES public.event_students(id) ON DELETE CASCADE,
    enrollment_no TEXT NOT NULL,
    marked_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    operator_name TEXT,
    device_id TEXT,
    status TEXT NOT NULL DEFAULT 'present' CHECK (status IN ('present', 'corrected')),
    marked_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    -- CRITICAL: Database-enforced uniqueness to guarantee zero duplicate attendance
    CONSTRAINT uq_attendance_event_enrollment UNIQUE (event_id, enrollment_no)
);

-- 6. Import Batches Table
CREATE TABLE IF NOT EXISTS public.import_batches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES public.events(id) ON DELETE CASCADE,
    imported_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    filename TEXT NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    invalid_rows INT NOT NULL DEFAULT 0,
    duplicate_rows INT NOT NULL DEFAULT 0,
    imported_rows INT NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'completed',
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- 7. Audit Logs Table
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES public.events(id) ON DELETE CASCADE,
    action TEXT NOT NULL,
    performed_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    target_id UUID,
    details JSONB DEFAULT '{}'::jsonb,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- Performance Indexes
CREATE INDEX IF NOT EXISTS idx_events_status ON public.events(status);
CREATE INDEX IF NOT EXISTS idx_event_students_event ON public.event_students(event_id);
CREATE INDEX IF NOT EXISTS idx_event_students_enrollment ON public.event_students(enrollment_no);
CREATE INDEX IF NOT EXISTS idx_event_students_name ON public.event_students(name);
CREATE INDEX IF NOT EXISTS idx_attendance_records_event ON public.attendance_records(event_id);
CREATE INDEX IF NOT EXISTS idx_attendance_records_marked_at ON public.attendance_records(marked_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_event ON public.audit_logs(event_id);
