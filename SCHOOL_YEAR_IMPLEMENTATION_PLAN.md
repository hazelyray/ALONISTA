# School Year Management & Re-Enrollment Implementation Plan

## Overview
This plan outlines the implementation of:
1. School Year 2025-2026 as the current school year
2. School year transition functionality (carrying over students)
3. Student re-enrollment/grade progression
4. School Year management UI in Settings menu

---

## Phase 1: Database Schema - School Year Entity

### 1.1 Create SchoolYear Model
**File:** `src/main/java/com/enrollment/system/model/SchoolYear.java`

**Fields:**
- `id` (Long, Primary Key)
- `year` (String, e.g., "2025-2026") - Unique
- `startDate` (LocalDate)
- `endDate` (LocalDate)
- `isCurrent` (Boolean) - Only one can be true at a time
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Key Features:**
- Unique constraint on `year`
- Only one `isCurrent = true` at any time (enforced in service layer)
- JPA lifecycle callbacks for timestamps

### 1.2 Update Student Model
**File:** `src/main/java/com/enrollment/system/model/Student.java`

**Add:**
- `schoolYear` (ManyToOne relationship to SchoolYear)
- `@JoinColumn(name = "school_year_id")`

**Migration Strategy:**
- Add nullable field initially
- Set all existing students to a default school year (2024-2025 or create it)
- Make it non-nullable after migration

### 1.3 Create SchoolYear Repository
**File:** `src/main/java/com/enrollment/system/repository/SchoolYearRepository.java`

**Methods:**
- `findByIsCurrentTrue()` - Get current school year
- `findByYear(String year)` - Find by year string
- `findAllByOrderByStartDateDesc()` - List all, newest first

---

## Phase 2: Service Layer - School Year Management

### 2.1 Create SchoolYearService
**File:** `src/main/java/com/enrollment/system/service/SchoolYearService.java`

**Key Methods:**

1. **`getCurrentSchoolYear()`**
   - Returns the school year marked as current
   - Throws exception if none exists

2. **`createSchoolYear(String year, LocalDate startDate, LocalDate endDate)`**
   - Creates new school year
   - Validates year format (YYYY-YYYY)
   - Validates date ranges
   - Sets `isCurrent = false` by default

3. **`setCurrentSchoolYear(Long schoolYearId)`**
   - Sets the specified school year as current
   - **CRITICAL:** Unsets all other school years' `isCurrent` flag
   - **CRITICAL:** Does NOT delete or archive existing students
   - Returns the newly set current school year

4. **`getAllSchoolYears()`**
   - Returns all school years ordered by start date (newest first)

5. **`getSchoolYearById(Long id)`**
   - Returns school year by ID

6. **`updateSchoolYear(Long id, SchoolYearDto dto)`**
   - Updates school year details
   - Prevents changing `isCurrent` directly (use `setCurrentSchoolYear`)

### 2.2 Update StudentService
**File:** `src/main/java/com/enrollment/system/service/StudentService.java`

**Changes:**

1. **Update `saveStudent()`**
   - Auto-assign current school year if not provided
   - Use `schoolYearService.getCurrentSchoolYear()`

2. **Update `getAllStudents()`**
   - Filter by current school year by default
   - Add optional parameter to filter by specific school year

3. **Add `getStudentsBySchoolYear(Long schoolYearId)`**
   - Returns all students (including archived) for a specific school year

4. **Add `reEnrollStudent(Long studentId, Integer newGradeLevel, Long newSectionId)`**
   - Creates enrollment record for new school year
   - Updates grade level
   - Updates section (if provided)
   - Sets enrollment status to "Pending" initially
   - Preserves student personal information

---

## Phase 3: School Year Transition Service

### 3.1 Create SchoolYearTransitionService
**File:** `src/main/java/com/enrollment/system/service/SchoolYearTransitionService.java`

**Key Methods:**

1. **`transitionToNewSchoolYear(Long newSchoolYearId, boolean carryOverEnrolled, boolean carryOverPending)`**
   - **CRITICAL:** This is the main transition method
   - Sets new school year as current
   - **Carries over students based on flags:**
     - If `carryOverEnrolled = true`: Copy all "Enrolled" students from previous year
     - If `carryOverPending = true`: Copy all "Pending" students from previous year
   - **For each carried-over student:**
     - Create new Student record with same personal info
     - Set new school year
     - **Increment grade level** (11 → 12, 12 → graduate/archive)
     - Set enrollment status to "Pending" (they need to be re-enrolled)
     - Clear section assignment (they need new section)
     - Preserve: name, LRN, contact info, strand, etc.
   - **Does NOT:**
     - Delete old student records (they remain for historical data)
     - Archive old students automatically
   - Returns transition summary (counts of students carried over)

2. **`getTransitionPreview(Long newSchoolYearId)`**
   - Preview what will happen during transition
   - Returns counts of students that will be carried over
   - Does not perform actual transition

3. **`reEnrollStudents(List<Long> studentIds, Integer gradeLevel, Long sectionId)`**
   - Batch re-enrollment for multiple students
   - Updates their enrollment status to "Enrolled"
   - Assigns section
   - Updates grade level if different

---

## Phase 4: DTOs and Controllers

### 4.1 Create SchoolYearDto
**File:** `src/main/java/com/enrollment/system/dto/SchoolYearDto.java`

**Fields:**
- All fields from SchoolYear model
- Helper methods for conversion

### 4.2 Update StudentDto
**File:** `src/main/java/com/enrollment/system/dto/StudentDto.java`

**Add:**
- `schoolYearId` (Long)
- `schoolYear` (String) - Display format "2025-2026"

**Update `fromStudent()` method:**
- Include school year information

### 4.3 Create SchoolYearController (REST API)
**File:** `src/main/java/com/enrollment/system/controller/SchoolYearController.java`

**Endpoints:**
- `GET /api/school-years/current` - Get current school year
- `GET /api/school-years` - List all school years
- `POST /api/school-years` - Create new school year
- `PUT /api/school-years/{id}/set-current` - Set as current
- `POST /api/school-years/transition` - Transition to new school year
- `GET /api/school-years/transition/preview` - Preview transition

### 4.4 Create ReEnrollmentController (REST API)
**File:** `src/main/java/com/enrollment/system/controller/ReEnrollmentController.java`

**Endpoints:**
- `POST /api/students/re-enroll` - Re-enroll single student
- `POST /api/students/batch-re-enroll` - Batch re-enrollment
- `GET /api/students/re-enrollable` - Get list of students eligible for re-enrollment

---

## Phase 5: UI Implementation - School Year Management

### 5.1 Create SchoolYearManagement FXML
**File:** `src/main/resources/FXML/SchoolYearManagement.fxml`

**UI Components:**
- TableView showing all school years
- Columns: Year, Start Date, End Date, Status (Current/Inactive)
- "Add New School Year" button
- "Set as Current" button (for selected school year)
- "Transition to New Year" button (opens transition dialog)

**Transition Dialog:**
- Select new school year
- Checkboxes:
  - ☑ Carry over Enrolled students
  - ☑ Carry over Pending students
- Preview section (shows counts)
- "Confirm Transition" button

### 5.2 Create SchoolYearManagementController
**File:** `src/main/java/com/enrollment/system/controller/SchoolYearManagementController.java`

**Features:**
- Load and display all school years
- Highlight current school year
- Add new school year form
- Set school year as current
- Transition wizard with preview
- Validation and error handling

### 5.3 Update DashboardController
**File:** `src/main/java/com/enrollment/system/controller/DashboardController.java`

**Changes:**
- Update `showSystemSettings()` method
- Add new button in Admin Settings submenu: "Add School Year"
- Link to SchoolYearManagement window

### 5.4 Update Dashboard FXML
**File:** `src/main/resources/FXML/Dashboard.fxml`

**Changes:**
- Add button in `adminSettingsSubmenu`:
  ```xml
  <Button fx:id="btnSchoolYearManagement" text="School Year Management" 
          onAction="#showSchoolYearManagement"
          style="..."/>
  ```

---

## Phase 6: Re-Enrollment UI

### 6.1 Add Re-Enrollment to ViewStudents
**Option A:** Add re-enrollment button in ViewStudentsController
**Option B:** Create separate ReEnrollmentController window

**Recommended:** Create separate window for better UX

### 6.2 Create ReEnrollment FXML
**File:** `src/main/resources/FXML/ReEnrollment.fxml`

**UI Components:**
- TableView of students eligible for re-enrollment
  - Filter: Show only students from previous school year
  - Columns: Name, Current Grade, Strand, Status
- Selection: Checkboxes or multi-select
- Grade Level selector (default: current + 1)
- Section selector (filtered by strand and grade)
- "Re-Enroll Selected" button
- Batch operations support

### 6.3 Create ReEnrollmentController
**File:** `src/main/java/com/enrollment/system/controller/ReEnrollmentController.java`

**Features:**
- Load students from previous school year
- Filter by grade level, strand
- Select multiple students
- Update grade level and section
- Batch re-enrollment
- Validation (grade level limits, section capacity)

---

## Phase 7: Data Migration & Initialization

### 7.1 Update DataInitializer
**File:** `src/main/java/com/enrollment/system/config/DataInitializer.java`

**Changes:**
1. Create default school year "2024-2025" (or detect from current date)
2. Set it as current
3. Assign all existing students to this school year
4. Create "2025-2026" school year (not current initially)
5. User can then transition to 2025-2026 when ready

**Migration Logic:**
```java
// If no school years exist, create default
if (schoolYearRepository.count() == 0) {
    // Create 2024-2025
    SchoolYear sy2024 = new SchoolYear();
    sy2024.setYear("2024-2025");
    sy2024.setStartDate(LocalDate.of(2024, 6, 1));
    sy2024.setEndDate(LocalDate.of(2025, 3, 31));
    sy2024.setIsCurrent(true);
    schoolYearRepository.save(sy2024);
    
    // Assign all existing students to 2024-2025
    List<Student> students = studentRepository.findAll();
    for (Student s : students) {
        s.setSchoolYear(sy2024);
    }
    studentRepository.saveAll(students);
    
    // Create 2025-2026 (not current)
    SchoolYear sy2025 = new SchoolYear();
    sy2025.setYear("2025-2026");
    sy2025.setStartDate(LocalDate.of(2025, 6, 1));
    sy2025.setEndDate(LocalDate.of(2026, 3, 31));
    sy2025.setIsCurrent(false);
    schoolYearRepository.save(sy2025);
}
```

---

## Phase 8: Backward Compatibility & Safety

### 8.1 Query Updates
**Update all student queries to:**
- Default to current school year
- Add optional school year filter
- Maintain backward compatibility (if schoolYear is null, include in results)

### 8.2 Service Layer Safety
- All student operations default to current school year
- Add validation: Cannot delete current school year if it has students
- Add validation: Cannot set school year as current if dates overlap incorrectly

### 8.3 Error Handling
- Clear error messages for school year operations
- Validation for date ranges
- Prevention of duplicate school years

---

## Implementation Order (Recommended)

1. **Phase 1** - Database schema (SchoolYear model, update Student)
2. **Phase 7** - Data migration (assign existing students)
3. **Phase 2** - Service layer (SchoolYearService, update StudentService)
4. **Phase 3** - Transition service
5. **Phase 4** - DTOs and REST controllers
6. **Phase 5** - UI (School Year Management)
7. **Phase 6** - Re-Enrollment UI
8. **Phase 8** - Testing and safety checks

---

## Testing Checklist

- [ ] Create school year 2025-2026
- [ ] Set 2025-2026 as current
- [ ] Verify existing students still visible (assigned to old year)
- [ ] Transition to 2025-2026 with carry-over
- [ ] Verify students carried over with incremented grade
- [ ] Verify old student records preserved
- [ ] Re-enroll students in new school year
- [ ] Verify section assignments work
- [ ] Test grade level progression (11 → 12)
- [ ] Test filtering by school year
- [ ] Test that new enrollments use current school year
- [ ] Verify no data loss during transition

---

## Key Design Decisions

1. **Student Records:** Keep historical records, don't delete
2. **School Year Assignment:** All students must belong to a school year
3. **Current School Year:** Only one can be current at a time
4. **Transition:** Explicit action, not automatic
5. **Re-Enrollment:** Separate process from transition (transition carries over, re-enrollment activates)
6. **Grade Progression:** Automatic during transition (11→12, 12→graduate/archive)

---

## Notes

- This plan ensures **no breaking changes** to existing functionality
- All existing students will be assigned to a default school year during migration
- The system will continue to work normally, just with school year awareness
- Transition is a manual, controlled process to prevent accidental data issues

