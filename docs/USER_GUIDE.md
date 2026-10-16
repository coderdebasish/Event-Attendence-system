# User & Operator Guide: Event Attendance System

This guide explains how attendance operators and administrators use the **Event Attendance System** Android application during college events, hackathons, and workshops.

---

## 1. Operator Workflow

### Step 1: Onboarding
- On first launch, enter your **Display Name** (e.g., `Alex Sharma`).
- Tap **Continue to Events**.
- Your name is saved automatically in local preferences and attributed to every attendance record you mark.

### Step 2: Select an Event
- From the event list, tap on the assigned event (e.g., `Annual Tech Symposium 2026`).
- The application automatically preloads the event's registered student roster into local memory for instant sub-50ms search.

### Step 3: Unified Attendance Workspace
On the workspace screen, three attendance marking methods are available on a single screen:

#### A. Search by Enrollment Number
- Type digits of the student's enrollment number (e.g., `229`).
- The list filters immediately as you type.
- **Auto-Navigation**: When you type the full unique enrollment number (e.g., `2292`), the student's profile opens automatically without requiring a tap on the list.
- Tap **Mark Attendance** to record.

#### B. Search by Student Name
- Type the student's name (e.g., `Aarav`).
- Search is case-insensitive and normalized.
- If you type the complete unique full name, the profile opens directly. If multiple students share the same name, a candidate list is shown for selection.

#### C. Continuous QR & Barcode Scanner
- Tap the **Open Scanner** button.
- Point the camera at the student's ID card QR code.
- **Auto-Confirmation**:
  - Valid attendance: Emits a subtle tone, displays the student's name & enrollment number in a green banner for ~1 second, and automatically resumes scanning.
  - Already marked: Displays an amber banner with the original time and operator name.
  - Not registered: Displays a red alert indicating the student is not registered for this event.

---

## 2. Administrator Workflow

### Step 1: Admin Login
- On onboarding or in settings, tap **Switch to Administrator Login**.
- Enter admin credentials (e.g., `admin@college.edu` / `admin123`).

### Step 2: Event Management
- Tap the floating **+** button to create a new event.
- Set title, description, date, status (`open`, `closed`, `archived`), and toggle attendance permission.

### Step 3: Importing Student Registration Rosters
1. From the Admin Dashboard, tap **Import Roster**.
2. Select any `.xlsx`, `.xls`, or `.csv` spreadsheet file.
3. Configure column mappings for the 6 fields:
   - **Name**: Required
   - **Enrollment Number**: Required
   - **Section, Roll Number, Email, Mobile**: Optional (can be set to "Not Mapped")
4. Tap **Validate & Preview Rows** to inspect valid rows, duplicates, and errors.
5. Select conflict strategy (`Skip Existing` or `Update Details`).
6. Tap **Confirm & Commit Import**.

### Step 4: Live Monitoring, Audit Trail & Export
- Tap **Live Monitoring** to see live attendance statistics.
- Tap **Export Attendance as CSV / Excel** to share the complete attendee report.
- To correct a mistake, tap the edit icon next to any record, provide an audit reason, and submit. The correction is recorded in the tamper-evident **Audit Trail**.
