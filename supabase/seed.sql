-- ============================================================================
-- Event Attendance System - seed.sql
-- Development seed data with realistic college student rosters and sample event
-- ============================================================================

-- Insert Sample Event
INSERT INTO public.events (id, title, description, event_date, status, allow_attendance)
VALUES (
    'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
    'Annual Tech Symposium 2026',
    'National level technical workshops, hackathons, and research paper presentations.',
    NOW() + INTERVAL '1 day',
    'open',
    true
) ON CONFLICT (id) DO NOTHING;

-- Insert Master Students
INSERT INTO public.students (id, enrollment_no, name, section, roll_no, email, mobile)
VALUES
    ('11111111-1111-1111-1111-111111111111', '12025052002292', 'Aarav Sharma', 'A', '101', 'aarav.sharma@example.edu', '+919876543210'),
    ('22222222-2222-2222-2222-222222222222', '00123456789012', 'Ananya Das', 'A', '102', 'ananya.das@example.edu', '+919876543211'),
    ('33333333-3333-3333-3333-333333333333', '2294', 'Arjun Roy', 'B', '205', 'arjun.roy@example.edu', '+919876543212'),
    ('44444444-4444-4444-4444-444444444444', '2301', 'Sneha Patel', 'B', '210', 'sneha.patel@example.edu', '+919876543213'),
    ('55555555-5555-5555-5555-555555555555', '2292', 'Rohan Verma', 'C', '304', 'rohan.verma@example.edu', '+919876543214')
ON CONFLICT (enrollment_no) DO NOTHING;

-- Register Students to the Event Roster
INSERT INTO public.event_students (event_id, student_id, enrollment_no, name, section, roll_no, email, mobile)
VALUES
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '11111111-1111-1111-1111-111111111111', '12025052002292', 'Aarav Sharma', 'A', '101', 'aarav.sharma@example.edu', '+919876543210'),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '22222222-2222-2222-2222-222222222222', '00123456789012', 'Ananya Das', 'A', '102', 'ananya.das@example.edu', '+919876543211'),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '33333333-3333-3333-3333-333333333333', '2294', 'Arjun Roy', 'B', '205', 'arjun.roy@example.edu', '+919876543212'),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '44444444-4444-4444-4444-444444444444', '2301', 'Sneha Patel', 'B', '210', 'sneha.patel@example.edu', '+919876543213'),
    ('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d', '55555555-5555-5555-5555-555555555555', '2292', 'Rohan Verma', 'C', '304', 'rohan.verma@example.edu', '+919876543214')
ON CONFLICT (event_id, enrollment_no) DO NOTHING;
