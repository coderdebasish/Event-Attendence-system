-- ============================================================================
-- Event Attendance System - 002_rls_policies.sql
-- Row Level Security (RLS) policies enforcing role-based permissions
-- ============================================================================

-- Helper function to check if current user is admin
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid() AND role = 'admin'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.students ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.event_students ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance_records ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.import_batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- ----------------------------------------------------------------------------
-- PROFILES POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Profiles are viewable by authenticated users"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Enable insert for authenticated users or system"
    ON public.profiles FOR INSERT
    WITH CHECK (true);

CREATE POLICY "Users can update their own profile name"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id AND (role = (SELECT role FROM public.profiles WHERE id = auth.uid()) OR public.is_admin()));

-- ----------------------------------------------------------------------------
-- EVENTS POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Operators can view published or open events"
    ON public.events FOR SELECT
    TO authenticated
    USING (status IN ('published', 'open', 'closed') OR public.is_admin());

CREATE POLICY "Admins can insert events"
    ON public.events FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "Admins can update events"
    ON public.events FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Admins can delete events"
    ON public.events FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ----------------------------------------------------------------------------
-- STUDENTS DIRECTORY POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Authenticated users can view students"
    ON public.students FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Admins can manage master students"
    ON public.students FOR ALL
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- ----------------------------------------------------------------------------
-- EVENT STUDENTS (ROSTER) POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Authenticated users can view event roster"
    ON public.event_students FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Admins can insert event students"
    ON public.event_students FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "Admins can update event students"
    ON public.event_students FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Admins can delete event students"
    ON public.event_students FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ----------------------------------------------------------------------------
-- ATTENDANCE RECORDS POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Authenticated users can view attendance records"
    ON public.attendance_records FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "Authenticated users can insert attendance"
    ON public.attendance_records FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() IS NOT NULL);

CREATE POLICY "Admins can update attendance"
    ON public.attendance_records FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "Admins can delete attendance"
    ON public.attendance_records FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ----------------------------------------------------------------------------
-- IMPORT BATCHES POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Admins can view import batches"
    ON public.import_batches FOR SELECT
    TO authenticated
    USING (public.is_admin());

CREATE POLICY "Admins can insert import batches"
    ON public.import_batches FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

-- ----------------------------------------------------------------------------
-- AUDIT LOGS POLICIES
-- ----------------------------------------------------------------------------
CREATE POLICY "Admins can view audit logs"
    ON public.audit_logs FOR SELECT
    TO authenticated
    USING (public.is_admin());

CREATE POLICY "Allow server-level audit insertions"
    ON public.audit_logs FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() IS NOT NULL);
