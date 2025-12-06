# Semester Implementation Plan
## Adding Semester Specification to School Year Without Breaking Existing Code

---

## Overview

This plan implements automatic semester creation (2 semesters per grade: Grade 11 Sem 1, Grade 11 Sem 2, Grade 12 Sem 1, Grade 12 Sem 2) when a school year is added, and integrates semester selection into the "Add New Student" form.

**Key Requirements:**
- Every school year automatically includes 2 semesters per grade (4 total: G11-S1, G11-S2, G12-S1, G12-S2)
- Semesters must be available in "Add New Student" dropdown
- Must not break existing code, queries, or student records
- Backward compatibility: existing students without semesters should still work

---

## Phase 1: Database Schema - Semester Entity

### 1.1 Create Semester Model
**File:** `src/main/java/com/enrollment/system/model/Semester.java`

**Fields:**
- `id` (Long, Primary Key)
- `schoolYear` (ManyToOne relationship to SchoolYear) - Required
- `gradeLevel` (Integer) - 11 or 12 - Required
- `semesterNumber` (Integer) - 1 or 2 - Required
- `name` (String) - e.g., "Grade 11 - Semester 1" - Computed/Generated
- `isActive` (Boolean) - Default true
- `createdAt` (LocalDateTime)
- `updatedAt` (LocalDateTime)

**Key Features:**
- Unique constraint: `(schoolYear, gradeLevel, semesterNumber)` - ensures no duplicates
- JPA lifecycle callbacks for timestamps
- Relationship: ManyToOne with SchoolYear (cascade delete when school year is deleted)

**Database Migration Strategy:**
- New table `semesters` will be created automatically by JPA
- Existing students will have `null` semester (backward compatible)
- No data migration needed for existing records

---

## Phase 2: Repository Layer

### 2.1 Create SemesterRepository
**File:** `src/main/java/com/enrollment/system/repository/SemesterRepository.java`

**Methods:**
- `findBySchoolYearId(Long schoolYearId)` - Get all semesters for a school year
- `findBySchoolYearIdAndGradeLevel(Long schoolYearId, Integer gradeLevel)` - Get semesters for specific grade
- `findBySchoolYearIdAndGradeLevelAndSemesterNumber(Long schoolYearId, Integer gradeLevel, Integer semesterNumber)` - Get specific semester
- `existsBySchoolYearIdAndGradeLevelAndSemesterNumber(Long schoolYearId, Integer gradeLevel, Integer semesterNumber)` - Check if exists
- `findAllByOrderBySchoolYearStartDateDescGradeLevelAscSemesterNumberAsc()` - List all, ordered

---

## Phase 3: Service Layer

### 3.1 Create SemesterService
**File:** `src/main/java/com/enrollment/system/service/SemesterService.java`

**Key Methods:**

1. **`createSemestersForSchoolYear(Long schoolYearId)`**
   - Automatically creates 4 semesters:
     - Grade 11 - Semester 1
     - Grade 11 - Semester 2
     - Grade 12 - Semester 1
     - Grade 12 - Semester 2
   - Called automatically when school year is created
   - Returns list of created semesters

2. **`getSemestersBySchoolYear(Long schoolYearId)`**
   - Returns all semesters for a school year
   - Ordered by grade level, then semester number

3. **`getSemestersBySchoolYearAndGrade(Long schoolYearId, Integer gradeLevel)`**
   - Returns semesters for specific grade in a school year

4. **`getAllSemestersForDropdown()`**
   - Returns formatted list for dropdown: "2025-2026 - Grade 11 - Semester 1"
   - Ordered by school year (newest first), grade, semester

5. **`getSemesterById(Long id)`**
   - Get specific semester

**Integration with SchoolYearService:**
- Modify `SchoolYearService.createSchoolYear()` to call `semesterService.createSemestersForSchoolYear()` after saving school year

---

### 3.2 Update SchoolYearService
**File:** `src/main/java/com/enrollment/system/service/SchoolYearService.java`

**Changes:**
- Inject `SemesterService` (optional to maintain backward compatibility)
- In `createSchoolYear()` method, after saving school year:
  ```java
  SchoolYear saved = schoolYearRepository.save(schoolYear);
  // Auto-create semesters
  if (semesterService != null) {
      semesterService.createSemestersForSchoolYear(saved.getId());
  }
  return SchoolYearDto.fromSchoolYear(saved);
  ```

**Backward Compatibility:**
- Make `SemesterService` injection optional (`@Autowired(required = false)`)
- If service is not available, school year creation still works (just no semesters)

---

## Phase 4: Update Student Model and DTO

### 4.1 Update Student Model
**File:** `src/main/java/com/enrollment/system/model/Student.java`

**Add:**
- `semester` (ManyToOne relationship to Semester) - **Nullable** for backward compatibility
- `@JoinColumn(name = "semester_id")`

**Important:**
- Field must be nullable to support existing students
- Lazy loading (FetchType.LAZY) for performance

---

### 4.2 Update StudentDto
**File:** `src/main/java/com/enrollment/system/dto/StudentDto.java`

**Add:**
- `semesterId` (Long)
- `semesterName` (String) - e.g., "Grade 11 - Semester 1"
- `semesterDisplayName` (String) - e.g., "2025-2026 - Grade 11 - Semester 1"

**Update `fromStudent()` method:**
- Safely extract semester information with try-catch for lazy loading
- Format display name: `schoolYear + " - " + semesterName`

---

## Phase 5: Update StudentService

### 5.1 Update StudentService
**File:** `src/main/java/com/enrollment/system/service/StudentService.java`

**Changes:**

1. **Inject SemesterRepository** (optional for backward compatibility)

2. **Update `saveStudent()` method:**
   - If `studentDto.getSemesterId()` is provided:
     - Validate semester exists
     - Validate semester belongs to the school year being used
     - Set semester on student
   - If not provided: set to null (backward compatible)

3. **Update `updateStudent()` method:**
   - Same semester handling as `saveStudent()`

**Validation Logic:**
- If semester is provided, ensure it matches the student's school year
- Semester is optional (nullable) - existing code continues to work

---

## Phase 6: Frontend - Add Student Form

### 6.1 Update AddStudent.fxml
**File:** `src/main/resources/FXML/AddStudent.fxml`

**Add Semester Dropdown:**
- Add new ComboBox after "Enrollment Status" field
- Label: "School Year & Semester *"
- fx:id: `semesterComboBox`
- Position: In Academic Information section, after Enrollment Status

**Layout:**
- Add to GridPane row 3 (after Enrollment Status)
- Column span: 2 columns for better visibility

---

### 6.2 Update AddStudentController
**File:** `src/main/java/com/enrollment/system/controller/AddStudentController.java`

**Changes:**

1. **Add FXML Field:**
   ```java
   @FXML
   private ComboBox<String> semesterComboBox;
   ```

2. **Add Service Injection:**
   ```java
   @Autowired(required = false)
   private SemesterService semesterService;
   ```

3. **Add Data Structure for Semester Selection:**
   - Create inner class or use Map to store: displayName -> semesterId
   - Example: `Map<String, Long> semesterMap = new HashMap<>();`

4. **Add Method `loadSemesters()`:**
   - Load all semesters from all school years
   - Format: "2025-2026 - Grade 11 - Semester 1"
   - Populate ComboBox and maintain mapping

5. **Update `initialize()` method:**
   - Call `loadSemesters()` after other initializations
   - Set up ComboBox cell factory for proper display

6. **Update `handleSave()` method:**
   - Validate semester selection (required field)
   - Extract semesterId from selection
   - Set `studentDto.setSemesterId(semesterId)`

7. **Update `populateFormFromStudent()` method:**
   - If student has semester, set ComboBox value to formatted display name

8. **Update `clearAllFields()` method:**
   - Clear semesterComboBox

**Validation:**
- Semester is required for new students
- Display error if not selected

---

## Phase 7: Create SemesterDto (Optional but Recommended)

### 7.1 Create SemesterDto
**File:** `src/main/java/com/enrollment/system/dto/SemesterDto.java`

**Fields:**
- `id` (Long)
- `schoolYearId` (Long)
- `schoolYear` (String) - e.g., "2025-2026"
- `gradeLevel` (Integer)
- `semesterNumber` (Integer)
- `name` (String) - e.g., "Grade 11 - Semester 1"
- `displayName` (String) - e.g., "2025-2026 - Grade 11 - Semester 1"
- `isActive` (Boolean)

**Use Cases:**
- API responses
- Cleaner data transfer
- Better separation of concerns

---

## Phase 8: Testing & Backward Compatibility

### 8.1 Backward Compatibility Checklist

**Existing Students:**
- ✅ Students without semester (null) should still display correctly
- ✅ Student listing should work with or without semester
- ✅ Student filtering should work regardless of semester
- ✅ Archive/restore should work with null semester

**Existing Queries:**
- ✅ All existing repository queries should work
- ✅ StudentService methods should handle null semester gracefully
- ✅ No breaking changes to existing API endpoints

**School Year Management:**
- ✅ Creating school year without semester service should still work
- ✅ Existing school years (without semesters) should not cause errors
- ✅ Setting current school year should work as before

### 8.2 Migration Strategy for Existing Data

**Option 1: Leave Existing Students as Null (Recommended)**
- Existing students keep `semester_id = null`
- Only new enrollments require semester
- Simplest and safest approach

**Option 2: Backfill Semesters (If Needed)**
- Create migration script to:
  1. Find all school years without semesters
  2. Create semesters for those school years
  3. Assign default semester to existing students (e.g., Semester 1 of their grade)
- **Only implement if explicitly requested**

---

## Phase 9: Implementation Order

### Step 1: Database & Models (Non-Breaking)
1. Create `Semester` model
2. Create `SemesterRepository`
3. Update `Student` model (add nullable semester field)
4. Update `StudentDto` (add semester fields)

### Step 2: Services (Backward Compatible)
1. Create `SemesterService`
2. Update `SchoolYearService` (optional injection)
3. Update `StudentService` (optional semester handling)

### Step 3: Frontend Integration
1. Update `AddStudent.fxml` (add ComboBox)
2. Update `AddStudentController` (load and handle semesters)

### Step 4: Testing
1. Test creating new school year (should auto-create semesters)
2. Test adding new student with semester selection
3. Test existing students (should still work with null semester)
4. Test all existing functionality (should not break)

---

## Phase 10: Additional Considerations

### 10.1 View Students Module
- Consider adding semester column to student listing
- Filter by semester (optional enhancement)

### 10.2 Reports & Analytics
- Semester-based enrollment statistics
- Semester-wise student counts

### 10.3 Re-Enrollment
- Re-enrollment should also allow semester selection
- Update `ReEnrollmentController` if needed

---

## Summary

**Database Changes:**
- New `semesters` table
- New `semester_id` column in `students` table (nullable)

**Code Changes:**
- New: `Semester` model, `SemesterRepository`, `SemesterService`, `SemesterDto`
- Modified: `SchoolYearService`, `Student` model, `StudentDto`, `StudentService`, `AddStudentController`, `AddStudent.fxml`

**Breaking Changes:**
- **None** - All changes are backward compatible

**New Features:**
- Automatic semester creation when school year is added
- Semester selection in "Add New Student" form
- Semester tracking for new enrollments

---

## Implementation Notes

1. **Null Safety:** Always check for null semester in all code paths
2. **Optional Dependencies:** Use `@Autowired(required = false)` for semester service to maintain compatibility
3. **Error Handling:** Gracefully handle cases where semester service is unavailable
4. **Validation:** Ensure semester belongs to selected school year
5. **UI/UX:** Format dropdown display clearly: "School Year - Grade - Semester"

---

## Next Steps After Implementation

1. Test thoroughly with existing data
2. Verify all existing functions work
3. Test new semester creation flow
4. Test new student enrollment with semester
5. Consider adding semester to other views (View Students, etc.)

