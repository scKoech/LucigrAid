package com.example.androidproject.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.androidproject.data.seedDatabase
import com.example.androidproject.model.Cohort
import com.example.androidproject.model.Course
import com.example.androidproject.model.GradeBoundaries
import com.example.androidproject.model.Group
import com.example.androidproject.model.Result
import com.example.androidproject.model.Student
import com.example.androidproject.model.StudentResult
import com.example.androidproject.model.Subject
import com.example.androidproject.model.University
import com.example.androidproject.model.WelfordStats
import com.example.androidproject.ui.management.GradeBoundaryRow
import com.example.androidproject.ui.management.ManagementModal
import com.example.androidproject.ui.management.StatisticRow
import com.example.androidproject.util.getDynamicGrade
import com.example.androidproject.util.getGradeColor
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCalculatorApp() {
    // State for management modal
    var showManagementModal by remember { mutableStateOf(false) }

    // Lifted selection state
    var selectedUni by remember { mutableStateOf<University?>(null) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedCohort by remember { mutableStateOf<Cohort?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

    // State for dropdowns expanded
    var courseExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

    // State for add student inputs
    var studentName by remember { mutableStateOf("") }
    var studentScore by remember { mutableStateOf("") }
    var saveResult by remember { mutableStateOf<String?>(null) }
    var lastGrade by remember { mutableStateOf<String?>(null) }
    var showSaveResult by remember { mutableStateOf(false) }
    var nameSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showNameSuggestions by remember { mutableStateOf(false) }

    // State for search
    var searchName by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<List<StudentResult>>(emptyList()) }
    var showSearchResult by remember { mutableStateOf(false) }
    var searchSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSearchSuggestions by remember { mutableStateOf(false) }

    // State for showing all students
    var showAllStudents by remember { mutableStateOf(false) }

    // Welford's statistics tracker
    val statsTracker = remember { WelfordStats() }

    // Lists to store hierarchy data
    val students = remember { mutableStateListOf<Student>() }
    val results = remember { mutableStateListOf<Result>() }
    val universities = remember { mutableStateListOf<University>() }
    val courses = remember { mutableStateListOf<Course>() }
    val cohorts = remember { mutableStateListOf<Cohort>() }
    val groups = remember { mutableStateListOf<Group>() }
    val subjects = remember { mutableStateListOf<Subject>() }

    // Current grade boundaries
    var gradeBoundaries by remember { mutableStateOf<GradeBoundaries?>(null) }

    // Initialize with seed data
    remember {
        seedDatabase(
            universities, courses, cohorts, groups, subjects, students, results, statsTracker
        )
        
        // Set initial state to first items
        selectedUni = universities.firstOrNull()
        selectedCourse = courses.firstOrNull { it.universityId == selectedUni?.id }
        selectedCohort = cohorts.firstOrNull { it.courseId == selectedCourse?.id }
        selectedGroup = groups.firstOrNull { it.cohortId == selectedCohort?.id }
        
        gradeBoundaries = statsTracker.getGradeBoundaries()
        true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "University Management",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showManagementModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Management"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Grade Calculator",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Course Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = courseExpanded,
                onExpandedChange = { courseExpanded = !courseExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedCourse?.name ?: "Select Course",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Course") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(16.dp)
                )

                ExposedDropdownMenu(
                    expanded = courseExpanded,
                    onDismissRequest = { courseExpanded = false }
                ) {
                    val filteredCourses = if (selectedUni != null) {
                        courses.filter { it.universityId == selectedUni?.id }
                    } else {
                        emptyList()
                    }

                    if (filteredCourses.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No courses available") },
                            onClick = { courseExpanded = false },
                            enabled = false
                        )
                    } else {
                        filteredCourses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.name) },
                                onClick = {
                                    selectedCourse = course
                                    selectedCohort = null
                                    selectedGroup = null
                                    courseExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Group Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = groupExpanded,
                onExpandedChange = { groupExpanded = !groupExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedGroup?.name ?: "Select Group",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(16.dp)
                )

                ExposedDropdownMenu(
                    expanded = groupExpanded,
                    onDismissRequest = { groupExpanded = false }
                ) {
                    val courseGroups = if (selectedCourse != null) {
                        val courseCohorts = cohorts.filter { it.courseId == selectedCourse?.id }
                        groups.filter { group -> courseCohorts.any { it.id == group.cohortId } }
                    } else {
                        emptyList()
                    }

                    if (courseGroups.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text(if (selectedCourse == null) "Select a Course first" else "No groups available") },
                            onClick = { groupExpanded = false },
                            enabled = false
                        )
                    } else {
                        courseGroups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group.name) },
                                onClick = {
                                    selectedGroup = group
                                    groupExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Add Student Section
            Text(
                text = "Add Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            if (selectedGroup == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Please select a Group before adding students",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name input field with suggestions
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = studentName,
                    onValueChange = { newName ->
                        studentName = newName
                        // Generate suggestions (filtered by group)
                        nameSuggestions = if (newName.isNotEmpty() && selectedGroup != null) {
                            students
                                .filter { it.groupId == selectedGroup?.id }
                                .map { it.name }
                                .filter { it.contains(newName, ignoreCase = true) }
                                .distinct()
                        } else {
                            emptyList()
                        }
                        showNameSuggestions = nameSuggestions.isNotEmpty()
                    },
                    label = { Text("Student Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedGroup != null
                )

                // Name suggestions dropdown
                if (showNameSuggestions && nameSuggestions.isNotEmpty()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            nameSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        studentName = suggestion
                                        showNameSuggestions = false
                                        nameSuggestions = emptyList()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Score input field
            OutlinedTextField(
                value = studentScore,
                onValueChange = { studentScore = it },
                label = { Text("Score (0-100)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedGroup != null
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Calculate & Save button
            Button(
                onClick = {
                    val score = studentScore.toIntOrNull()
                    if (studentName.isNotBlank() && score != null && score in 0..100 && selectedGroup != null) {
                        // Add score to statistics tracker
                        statsTracker.addValue(score.toDouble())

                        // Update grade boundaries
                        gradeBoundaries = statsTracker.getGradeBoundaries()

                        // Calculate grade using dynamic boundaries
                        val grade = getDynamicGrade(score.toDouble(), gradeBoundaries!!)
                        
                        val studentId = UUID.randomUUID().toString()
                        val student = Student(
                            id = studentId,
                            name = studentName,
                            regNumber = "REG-${(1000..9999).random()}",
                            groupId = selectedGroup!!.id
                        )
                        val result = Result(
                            studentId = studentId,
                            subjectId = subjects.firstOrNull { it.courseId == selectedCourse?.id }?.id ?: (subjects.firstOrNull()?.id ?: ""),
                            score = score,
                            grade = grade
                        )
                        
                        students.add(student)
                        results.add(result)
                        
                        saveResult = "Grade: $grade"
                        lastGrade = grade
                        showSaveResult = true
                        // Clear inputs
                        studentName = ""
                        studentScore = ""
                        showNameSuggestions = false
                    } else if (selectedGroup == null) {
                        saveResult = "Please select a group first."
                        lastGrade = null
                        showSaveResult = true
                    } else {
                        saveResult = "Invalid input. Please enter a valid name and score (0-100)."
                        lastGrade = null
                        showSaveResult = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedGroup != null
            ) {
                Text(
                    text = "Calculate & Save",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save result display
            AnimatedVisibility(
                visible = showSaveResult,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                saveResult?.let { text ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (text.startsWith("Grade:")) getGradeColor(lastGrade ?: "") else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Dynamic Grade Distribution Panel
            if (gradeBoundaries != null && statsTracker.getCount() > 0) {
                Text(
                    text = "Dynamic Grade Distribution",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Statistics
                        StatisticRow(
                            label = "Mean (μ)",
                            value = String.format("%.2f", gradeBoundaries!!.mean)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatisticRow(
                            label = "Std Dev (σ)",
                            value = String.format("%.2f", gradeBoundaries!!.stdDev)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        StatisticRow(
                            label = "Students",
                            value = statsTracker.getCount().toString()
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Grade Boundaries (z-score based)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Grade A
                        GradeBoundaryRow(
                            grade = "A",
                            threshold = "≥ ${String.format("%.2f", gradeBoundaries!!.aThreshold)}",
                            color = getGradeColor("A")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Grade B
                        GradeBoundaryRow(
                            grade = "B",
                            threshold = "${String.format("%.2f", gradeBoundaries!!.bLowerThreshold)} - ${String.format("%.2f", gradeBoundaries!!.bUpperThreshold)}",
                            color = getGradeColor("B")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Grade C
                        GradeBoundaryRow(
                            grade = "C",
                            threshold = "${String.format("%.2f", gradeBoundaries!!.cLowerThreshold)} - ${String.format("%.2f", gradeBoundaries!!.cUpperThreshold)}",
                            color = getGradeColor("C")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Grade D
                        GradeBoundaryRow(
                            grade = "D",
                            threshold = "${String.format("%.2f", gradeBoundaries!!.dLowerThreshold)} - ${String.format("%.2f", gradeBoundaries!!.dUpperThreshold)}",
                            color = getGradeColor("D")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Grade F
                        GradeBoundaryRow(
                            grade = "F",
                            threshold = "< ${String.format("%.2f", gradeBoundaries!!.dLowerThreshold)}",
                            color = getGradeColor("F")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }

            // View All Students Section
            Text(
                text = "All Students",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter students by selected group
            val filteredStudents = if (selectedGroup != null) {
                students.filter { it.groupId == selectedGroup?.id }
            } else {
                emptyList()
            }

            // Toggle button for showing all students
            OutlinedButton(
                onClick = {
                    showAllStudents = !showAllStudents
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (showAllStudents) "Hide All Students" else "Show All Students (${filteredStudents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // All Students List Display
            AnimatedVisibility(
                visible = showAllStudents,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                if (filteredStudents.isNotEmpty()) {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            // Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Name",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.5f)
                                )
                                Text(
                                    text = "Score",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.8f),
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "Grade",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.7f),
                                    textAlign = TextAlign.Center
                                )
                            }

                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            // Students List
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val studentsWithAverage = filteredStudents.map { student ->
                                        val studentResults = results.filter { it.studentId == student.id }
                                        val avg = if (studentResults.isNotEmpty()) studentResults.map { it.score }.average() else 0.0
                                        val grade = if (gradeBoundaries != null) getDynamicGrade(avg, gradeBoundaries!!) else "F"
                                        StudentResult(student, avg, grade)
                                    }
                                    items(studentsWithAverage) { studentResult ->
                                        val student = studentResult.student
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1.5f)) {
                                                Text(
                                                    text = student.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = student.regNumber,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = String.format("%.1f", studentResult.averageScore),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(0.8f),
                                                textAlign = TextAlign.Center
                                            )
                                            Text(
                                                text = studentResult.finalGrade,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = getGradeColor(studentResult.finalGrade),
                                                modifier = Modifier.weight(0.7f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (selectedGroup == null) "Select a group to see students." else "No students in this group.",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Search Student",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search name input field with suggestions
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchName,
                    onValueChange = { newSearchName ->
                        searchName = newSearchName
                        // Generate search suggestions (filtered by group)
                        searchSuggestions = if (newSearchName.isNotEmpty() && selectedGroup != null) {
                            students
                                .filter { it.groupId == selectedGroup?.id }
                                .map { it.name }
                                .filter { it.contains(newSearchName, ignoreCase = true) }
                                .distinct()
                        } else {
                            emptyList()
                        }
                        showSearchSuggestions = searchSuggestions.isNotEmpty()
                    },
                    label = { Text("Student Name") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedGroup != null
                )

                // Search suggestions dropdown
                if (showSearchSuggestions && searchSuggestions.isNotEmpty()) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            searchSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        searchName = suggestion
                                        showSearchSuggestions = false
                                        searchSuggestions = emptyList()
                                        // Automatically search (filtered by group)
                                        val foundStudents = students
                                            .filter { it.groupId == selectedGroup?.id }
                                            .filter { it.name.contains(searchName, ignoreCase = true) }
                                        searchResult = foundStudents.map { student ->
                                            val studentResults = results.filter { it.studentId == student.id }
                                            val avg = if (studentResults.isNotEmpty()) studentResults.map { it.score }.average() else 0.0
                                            val grade = if (gradeBoundaries != null) getDynamicGrade(avg, gradeBoundaries!!) else "F"
                                            StudentResult(student, avg, grade)
                                        }
                                        showSearchResult = true
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search button
            Button(
                onClick = {
                    val foundStudents = students
                        .filter { it.groupId == selectedGroup?.id }
                        .filter { it.name.contains(searchName, ignoreCase = true) }
                    searchResult = foundStudents.map { student ->
                        val studentResults = results.filter { it.studentId == student.id }
                        val avg = if (studentResults.isNotEmpty()) studentResults.map { it.score }.average() else 0.0
                        val grade = if (gradeBoundaries != null) getDynamicGrade(avg, gradeBoundaries!!) else "F"
                        StudentResult(student, avg, grade)
                    }
                    showSearchResult = true
                    showSearchSuggestions = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedGroup != null
            ) {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search result display
            AnimatedVisibility(
                visible = showSearchResult,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
            ) {
                if (searchResult.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        searchResult.forEach { studentResult ->
                            val student = studentResult.student
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Student: ${student.name}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = student.regNumber,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Average Score: ${String.format("%.2f", studentResult.averageScore)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Final Grade: ${studentResult.finalGrade}",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = getGradeColor(studentResult.finalGrade),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "No students found.",
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Management Modal Overlay using Dialog
    if (showManagementModal) {
        Dialog(
            onDismissRequest = { showManagementModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                ManagementModal(
                    onClose = { showManagementModal = false },
                    universities = universities,
                    courses = courses,
                    cohorts = cohorts,
                    groups = groups,
                    subjects = subjects,
                    selectedUni = selectedUni,
                    onUniSelected = { selectedUni = it },
                    selectedCourse = selectedCourse,
                    onCourseSelected = { selectedCourse = it },
                    selectedCohort = selectedCohort,
                    onCohortSelected = { selectedCohort = it },
                    selectedGroup = selectedGroup,
                    onGroupSelected = { selectedGroup = it }
                )
            }
        }
    }
}
