-- ============================================================================
-- Event Attendance System - 003_rpc_functions.sql
-- Concurrency-safe atomic attendance marking & correction procedures
-- ============================================================================

-- Function 1: Atomic Attendance Marking with Race-Condition Protection
CREATE OR REPLACE FUNCTION public.mark_attendance(
    p_event_id UUID,
    p_enrollment_no TEXT,
    p_operator_name TEXT DEFAULT 'Operator',
    p_device_id TEXT DEFAULT NULL
)
RETURNS JSONB AS $$
DECLARE
    v_event RECORD;
    v_event_student RECORD;
    v_existing_attendance RECORD;
    v_new_record RECORD;
    v_user_id UUID := auth.uid();
BEGIN
    -- 1. Validate Event exists and allows attendance
    SELECT id, title, status, allow_attendance INTO v_event
    FROM public.events
    WHERE id = p_event_id;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
            'status', 'EVENT_NOT_FOUND',
            'message', 'The specified event does not exist.'
        );
    END IF;

    IF NOT v_event.allow_attendance OR v_event.status NOT IN ('open', 'published') THEN
        RETURN jsonb_build_object(
            'status', 'EVENT_CLOSED',
            'message', 'Attendance marking is closed for this event.'
        );
    END IF;

    -- 2. Validate Student Registration in Event Roster
    SELECT id, student_id, name, enrollment_no, section, roll_no INTO v_event_student
    FROM public.event_students
    WHERE event_id = p_event_id AND enrollment_no = p_enrollment_no;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
            'status', 'STUDENT_NOT_REGISTERED',
            'message', 'Student with enrollment ' || p_enrollment_no || ' is not registered for this event.'
        );
    END IF;

    -- 3. Check for existing attendance record
    SELECT id, marked_at, operator_name INTO v_existing_attendance
    FROM public.attendance_records
    WHERE event_id = p_event_id AND enrollment_no = p_enrollment_no AND status = 'present';

    IF FOUND THEN
        RETURN jsonb_build_object(
            'status', 'ALREADY_MARKED',
            'message', 'Attendance was already marked.',
            'attendance_id', v_existing_attendance.id,
            'marked_at', v_existing_attendance.marked_at,
            'operator_name', v_existing_attendance.operator_name,
            'student_name', v_event_student.name,
            'enrollment_no', v_event_student.enrollment_no
        );
    END IF;

    -- 4. Insert new attendance record atomically
    BEGIN
        INSERT INTO public.attendance_records (
            event_id,
            event_student_id,
            enrollment_no,
            marked_by,
            operator_name,
            device_id,
            status,
            marked_at
        ) VALUES (
            p_event_id,
            v_event_student.id,
            v_event_student.enrollment_no,
            v_user_id,
            COALESCE(p_operator_name, 'Operator'),
            p_device_id,
            'present',
            timezone('utc'::text, now())
        )
        RETURNING * INTO v_new_record;

        RETURN jsonb_build_object(
            'status', 'SUCCESS',
            'message', 'Attendance marked successfully.',
            'attendance_id', v_new_record.id,
            'marked_at', v_new_record.marked_at,
            'operator_name', v_new_record.operator_name,
            'student_name', v_event_student.name,
            'enrollment_no', v_event_student.enrollment_no,
            'section', v_event_student.section,
            'roll_no', v_event_student.roll_no
        );
    EXCEPTION WHEN unique_violation THEN
        -- Handle simultaneous concurrent scan race condition gracefully
        SELECT id, marked_at, operator_name INTO v_existing_attendance
        FROM public.attendance_records
        WHERE event_id = p_event_id AND enrollment_no = p_enrollment_no;

        RETURN jsonb_build_object(
            'status', 'ALREADY_MARKED',
            'message', 'Attendance was already marked concurrently.',
            'attendance_id', v_existing_attendance.id,
            'marked_at', v_existing_attendance.marked_at,
            'operator_name', v_existing_attendance.operator_name,
            'student_name', v_event_student.name,
            'enrollment_no', v_event_student.enrollment_no
        );
    END;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function 2: Audit-Logged Attendance Correction (Admin Only)
CREATE OR REPLACE FUNCTION public.correct_attendance(
    p_attendance_id UUID,
    p_new_status TEXT,
    p_reason TEXT
)
RETURNS JSONB AS $$
DECLARE
    v_old RECORD;
    v_admin_id UUID := auth.uid();
BEGIN
    IF NOT public.is_admin() THEN
        RETURN jsonb_build_object(
            'status', 'PERMISSION_DENIED',
            'message', 'Only administrators can correct attendance records.'
        );
    END IF;

    SELECT * INTO v_old
    FROM public.attendance_records
    WHERE id = p_attendance_id;

    IF NOT FOUND THEN
        RETURN jsonb_build_object(
            'status', 'NOT_FOUND',
            'message', 'Attendance record not found.'
        );
    END IF;

    -- Update record
    UPDATE public.attendance_records
    SET status = p_new_status
    WHERE id = p_attendance_id;

    -- Insert into audit log
    INSERT INTO public.audit_logs (
        event_id,
        action,
        performed_by,
        target_id,
        details,
        reason
    ) VALUES (
        v_old.event_id,
        'CORRECT_ATTENDANCE',
        v_admin_id,
        p_attendance_id,
        jsonb_build_object(
            'enrollment_no', v_old.enrollment_no,
            'old_status', v_old.status,
            'new_status', p_new_status,
            'original_marked_at', v_old.marked_at
        ),
        p_reason
    );

    RETURN jsonb_build_object(
        'status', 'SUCCESS',
        'message', 'Attendance record corrected and audit entry logged.'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Enable Realtime for Events and Attendance Records
ALTER PUBLICATION supabase_realtime ADD TABLE public.events;
ALTER PUBLICATION supabase_realtime ADD TABLE public.attendance_records;
ALTER PUBLICATION supabase_realtime ADD TABLE public.event_students;
