package com.example.androidproject.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.androidproject.model.Subject
import com.example.androidproject.model.University
import com.example.androidproject.model.WelfordStats
import com.example.androidproject.ui.management.ManagementModal
import com.example.androidproject.util.getDynamicGrade
import com.example.androidproject.util.getGradeColor
import java.util.UUID
import kotlinx.coroutines.launch

enum class MainDestination(val title: String, val icon: ImageVector) {
    Dashboard("Dashboard", Icons.Default.Home),
    Roster("Roster", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var currentDestination by remember { mutableStateOf(MainDestination.Dashboard) }

    // State for management modal
    var showManagementModal by remember { mutableStateOf(false) }

    // State for Bottom Sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Lifted selection state
    var selectedUni by remember { mutableStateOf<University?>(null) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var selectedCohort by remember { mutableStateOf<Cohort?>(null) }
    var selectedGroup by remember { mutableStateOf<Group?>(null) }

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
        },
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination == destination,
                        onClick = { currentDestination = destination },
                        icon = { Icon(destination.icon, contentDescription = destination.title) },
                        label = { Text(destination.title) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showBottomSheet = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Student")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentDestination) {
                MainDestination.Dashboard -> DashboardScreen(
                    universities = universities,
                    courses = courses,
                    cohorts = cohorts,
                    groups = groups,
                    selectedUni = selectedUni,
                    selectedCourse = selectedCourse,
                    selectedCohort = selectedCohort,
                    selectedGroup = selectedGroup,
                    onUniSelected = { selectedUni = it },
                    onCourseSelected = { selectedCourse = it },
                    onCohortSelected = { selectedCohort = it },
                    onGroupSelected = { selectedGroup = it },
                    gradeBoundaries = gradeBoundaries,
                    studentCount = statsTracker.getCount()
                )
                MainDestination.Roster -> RosterScreen(
                    students = students,
                    results = results,
                    selectedGroup = selectedGroup,
                    gradeBoundaries = gradeBoundaries
                )
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            AddStudentForm(
                selectedGroup = selectedGroup,
                selectedCourse = selectedCourse,
                subjects = subjects,
                students = students,
                results = results,
                statsTracker = statsTracker,
                onGradeBoundariesUpdated = { gradeBoundaries = it },
                onDismiss = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showBottomSheet = false
                        }
                    }
                }
            )
        }
    }

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

@Composable
fun AddStudentForm(
    selectedGroup: Group?,
    selectedCourse: Course?,
    subjects: List<Subject>,
    students: MutableList<Student>,
    results: MutableList<Result>,
    statsTracker: WelfordStats,
    onGradeBoundariesUpdated: (GradeBoundaries?) -> Unit,
    onDismiss: () -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var studentScore by remember { mutableStateOf("") }
    var saveResult by remember { mutableStateOf<String?>(null) }
    var lastGrade by remember { mutableStateOf<String?>(null) }
    var showSaveResult by remember { mutableStateOf(false) }
    var nameSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showNameSuggestions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                            .filter { it.groupId == selectedGroup.id }
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
                    val gradeBoundaries = statsTracker.getGradeBoundaries()
                    onGradeBoundariesUpdated(gradeBoundaries)

                    // Calculate grade using dynamic boundaries
                    val grade = getDynamicGrade(score.toDouble(), gradeBoundaries!!)
                    
                    val studentId = UUID.randomUUID().toString()
                    val student = Student(
                        id = studentId,
                        name = studentName,
                        regNumber = "REG-${(1000..9999).random()}",
                        groupId = selectedGroup.id
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
    }
}
