package com.example.androidproject

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidproject.ui.theme.AndroidProjectTheme
import java.util.UUID
import kotlin.math.sqrt

// Hierarchy Data Models
data class University(val id: String, val name: String)
data class Course(val id: String, val name: String, val universityId: String)
data class Cohort(val id: String, val name: String, val courseId: String)
data class Group(val id: String, val name: String, val cohortId: String)
data class Subject(val id: String, val name: String, val courseId: String)
data class Student(val id: String, val name: String, val regNumber: String, val groupId: String)
data class Result(val studentId: String, val subjectId: String, val score: Int, val grade: String)

// Helper model for UI display
data class StudentResult(val student: Student, val averageScore: Double, val finalGrade: String)

// Data class to store grade boundaries
data class GradeBoundaries(
    val mean: Double,
    val stdDev: Double,
    val aThreshold: Double,
    val bUpperThreshold: Double,
    val bLowerThreshold: Double,
    val cUpperThreshold: Double,
    val cLowerThreshold: Double,
    val dUpperThreshold: Double,
    val dLowerThreshold: Double
)

// Welford's algorithm for incremental mean and variance calculation
class WelfordStats {
    private var count = 0
    private var mean = 0.0
    private var m2 = 0.0

    fun addValue(value: Double) {
        count++
        val delta = value - mean
        mean += delta / count
        val delta2 = value - mean
        m2 += delta * delta2
    }

    fun getCount() = count
    fun getMean() = mean

    fun getVariance(): Double {
        return if (count > 0) m2 / count else 0.0
    }

    fun getStdDev(): Double {
        return sqrt(getVariance())
    }

    fun getGradeBoundaries(): GradeBoundaries {
        val mu = getMean()
        val sigma = getStdDev()

        return GradeBoundaries(
            mean = mu.coerceIn(0.0, 100.0),
            stdDev = sigma,
            aThreshold = (mu + 1.5 * sigma).coerceIn(0.0, 100.0),
            bUpperThreshold = (mu + 1.5 * sigma).coerceIn(0.0, 100.0),
            bLowerThreshold = (mu + 0.5 * sigma).coerceIn(0.0, 100.0),
            cUpperThreshold = (mu + 0.5 * sigma).coerceIn(0.0, 100.0),
            cLowerThreshold = (mu - 0.5 * sigma).coerceIn(0.0, 100.0),
            dUpperThreshold = (mu - 0.5 * sigma).coerceIn(0.0, 100.0),
            dLowerThreshold = (mu - 1.5 * sigma).coerceIn(0.0, 100.0)
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidProjectTheme {
                GradeCalculatorApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeCalculatorApp() {
    // Gradient background colors
    val gradientColors = listOf(
        Color(0xFF667eea), // Blue
        Color(0xFF764ba2)  // Purple
    )

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "University Management",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        IconButton(onClick = { showManagementModal = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Management",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Grade Calculator",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
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
                        label = { Text("Course", color = Color.White) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = courseExpanded,
                        onDismissRequest = { courseExpanded = false },
                        modifier = Modifier.background(Color(0xFF2a2a3e))
                    ) {
                        val filteredCourses = if (selectedUni != null) {
                            courses.filter { it.universityId == selectedUni?.id }
                        } else {
                            emptyList()
                        }

                        if (filteredCourses.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No courses available for selected University", color = Color.White) },
                                onClick = { courseExpanded = false },
                                enabled = false
                            )
                        } else {
                            filteredCourses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.name, color = Color.White) },
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
                        label = { Text("Group", color = Color.White) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedLabelColor = Color.White,
                            unfocusedLabelColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(16.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = groupExpanded,
                        onDismissRequest = { groupExpanded = false },
                        modifier = Modifier.background(Color(0xFF2a2a3e))
                    ) {
                        val courseGroups = if (selectedCourse != null) {
                            val courseCohorts = cohorts.filter { it.courseId == selectedCourse?.id }
                            groups.filter { group -> courseCohorts.any { it.id == group.cohortId } }
                        } else {
                            emptyList()
                        }

                        if (courseGroups.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text(if (selectedCourse == null) "Select a Course first" else "No groups available for this Course", color = Color.White) },
                                onClick = { groupExpanded = false },
                                enabled = false
                            )
                        } else {
                            courseGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name, color = Color.White) },
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
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )

                if (selectedGroup == null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Please select a Group before adding students",
                        color = Color.Yellow,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
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
                        label = { Text("Student Name", color = Color.White) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        enabled = selectedGroup != null
                    )

                    // Name suggestions dropdown
                    if (showNameSuggestions && nameSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2a2a3e)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                nameSuggestions.forEach { suggestion ->
                                    Button(
                                        onClick = {
                                            studentName = suggestion
                                            showNameSuggestions = false
                                            nameSuggestions = emptyList()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
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
                    label = { Text("Score (0-100)", color = Color.White) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGroup != null) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    enabled = selectedGroup != null
                ) {
                    Text(
                        text = "Calculate & Save",
                        fontSize = 18.sp,
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (text.startsWith("Grade:")) getGradeColor(lastGrade!!).copy(alpha = 0.9f) else Color.Red.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Dynamic Grade Distribution Panel
                if (gradeBoundaries != null && statsTracker.getCount() > 0) {
                    Text(
                        text = "Dynamic Grade Distribution",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
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
                                value = String.format("%.2f", gradeBoundaries!!.mean),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StatisticRow(
                                label = "Std Dev (σ)",
                                value = String.format("%.2f", gradeBoundaries!!.stdDev),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            StatisticRow(
                                label = "Students",
                                value = statsTracker.getCount().toString(),
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Grade Boundaries (z-score based)",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Grade A
                            GradeBoundaryRow(
                                grade = "A",
                                threshold = "≥ ${String.format("%.2f", gradeBoundaries!!.aThreshold)}",
                                color = Color.Green
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Grade B
                            GradeBoundaryRow(
                                grade = "B",
                                threshold = "${String.format("%.2f", gradeBoundaries!!.bLowerThreshold)} - ${String.format("%.2f", gradeBoundaries!!.bUpperThreshold)}",
                                color = Color.Blue
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Grade C
                            GradeBoundaryRow(
                                grade = "C",
                                threshold = "${String.format("%.2f", gradeBoundaries!!.cLowerThreshold)} - ${String.format("%.2f", gradeBoundaries!!.cUpperThreshold)}",
                                color = Color(0xFFFFA500) // Orange
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Grade D
                            GradeBoundaryRow(
                                grade = "D",
                                threshold = "${String.format("%.2f", gradeBoundaries!!.dLowerThreshold)} - ${String.format("%.2f", gradeBoundaries!!.dUpperThreshold)}",
                                color = Color.Magenta // Purple
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Grade F
                            GradeBoundaryRow(
                                grade = "F",
                                threshold = "< ${String.format("%.2f", gradeBoundaries!!.dLowerThreshold)}",
                                color = Color.Red
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }

                // View All Students Section
                Text(
                    text = "All Students",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
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
                Button(
                    onClick = {
                        showAllStudents = !showAllStudents
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (showAllStudents) "Hide All Students" else "Show All Students (${filteredStudents.size})",
                        fontSize = 18.sp,
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
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.05f)
                            ),
                            elevation = CardDefaults.cardElevation(8.dp)
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
                                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Name",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.weight(1.5f)
                                    )
                                    Text(
                                        text = "Score",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.weight(0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Grade",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        modifier = Modifier.weight(0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }

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
                                                    .background(
                                                        getGradeColor(studentResult.finalGrade).copy(alpha = 0.15f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1.5f)) {
                                                    Text(
                                                        text = student.name,
                                                        style = MaterialTheme.typography.bodySmall.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = Color.White
                                                        ),
                                                        maxLines = 1
                                                    )
                                                    Text(
                                                        text = student.regNumber,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = Color.White.copy(alpha = 0.6f)
                                                        )
                                                    )
                                                }
                                                Text(
                                                    text = String.format("%.1f", studentResult.averageScore),
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    ),
                                                    modifier = Modifier.weight(0.8f),
                                                    textAlign = TextAlign.Center
                                                )
                                                Text(
                                                    text = studentResult.finalGrade,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = getGradeColor(studentResult.finalGrade)
                                                    ),
                                                    modifier = Modifier.weight(0.7f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Text(
                                text = if (selectedGroup == null) "Select a group to see students." else "No students in this group.",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "Search Student",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
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
                        label = { Text("Student Name", color = Color.White) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        enabled = selectedGroup != null
                    )

                    // Search suggestions dropdown
                    if (showSearchSuggestions && searchSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF2a2a3e)
                            ),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                searchSuggestions.forEach { suggestion ->
                                    Button(
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
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.fillMaxWidth(),
                                            textAlign = TextAlign.Start
                                        )
                                    }
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedGroup != null) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.5f),
                        contentColor = Color.White
                    ),
                    enabled = selectedGroup != null
                ) {
                    Text(
                        text = "Search",
                        fontSize = 18.sp,
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
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = getGradeColor(studentResult.finalGrade).copy(alpha = 0.9f)
                                    ),
                                    elevation = CardDefaults.cardElevation(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Student: ${student.name}",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = student.regNumber,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = Color.White.copy(alpha = 0.8f)
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Average Score: ${String.format("%.2f", studentResult.averageScore)}",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Final Grade: ${studentResult.finalGrade}",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Gray.copy(alpha = 0.9f)
                            ),
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Text(
                                text = "No students found.",
                                modifier = Modifier.padding(24.dp),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Management Modal Overlay
        AnimatedVisibility(
            visible = showManagementModal,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
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

@Composable
fun ManagementModal(
    onClose: () -> Unit,
    universities: MutableList<University>,
    courses: MutableList<Course>,
    cohorts: MutableList<Cohort>,
    groups: MutableList<Group>,
    subjects: MutableList<Subject>,
    selectedUni: University?,
    onUniSelected: (University?) -> Unit,
    selectedCourse: Course?,
    onCourseSelected: (Course?) -> Unit,
    selectedCohort: Cohort?,
    onCohortSelected: (Cohort?) -> Unit,
    selectedGroup: Group?,
    onGroupSelected: (Group?) -> Unit
) {
    var newUniName by remember { mutableStateOf("") }
    var newCourseName by remember { mutableStateOf("") }
    var newCohortName by remember { mutableStateOf("") }
    var newGroupName by remember { mutableStateOf("") }
    var newSubjectName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a2e))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Management",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onClose,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Universities Section
            ManagementSection(
                title = "Universities",
                items = universities.map { it.name },
                newItemValue = newUniName,
                onNewItemValueChange = { newUniName = it },
                onAddClick = {
                    if (newUniName.isNotBlank()) {
                        universities.add(University(UUID.randomUUID().toString(), newUniName))
                        newUniName = ""
                    }
                },
                selectedItemIndex = universities.indexOf(selectedUni),
                onItemClick = { index ->
                    val clickedUni = universities[index]
                    if (selectedUni == clickedUni) {
                        onUniSelected(null)
                        onCourseSelected(null)
                        onCohortSelected(null)
                        onGroupSelected(null)
                    } else {
                        onUniSelected(clickedUni)
                        onCourseSelected(null)
                        onCohortSelected(null)
                        onGroupSelected(null)
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))

            // Courses Section (Filtered by University)
            if (selectedUni != null) {
                val filteredCourses = courses.filter { it.universityId == selectedUni.id }
                ManagementSection(
                    title = "Courses (${selectedUni.name})",
                    items = filteredCourses.map { it.name },
                    newItemValue = newCourseName,
                    onNewItemValueChange = { newCourseName = it },
                    onAddClick = {
                        if (newCourseName.isNotBlank()) {
                            courses.add(Course(UUID.randomUUID().toString(), newCourseName, selectedUni.id))
                            newCourseName = ""
                        }
                    },
                    selectedItemIndex = filteredCourses.indexOf(selectedCourse),
                    onItemClick = { index ->
                        val clickedCourse = filteredCourses[index]
                        if (selectedCourse == clickedCourse) {
                            onCourseSelected(null)
                            onCohortSelected(null)
                            onGroupSelected(null)
                        } else {
                            onCourseSelected(clickedCourse)
                            onCohortSelected(null)
                            onGroupSelected(null)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            }

            // Cohorts Section (Filtered by Course)
            if (selectedCourse != null) {
                val filteredCohorts = cohorts.filter { it.courseId == selectedCourse.id }
                ManagementSection(
                    title = "Cohorts (${selectedCourse.name})",
                    items = filteredCohorts.map { it.name },
                    newItemValue = newCohortName,
                    onNewItemValueChange = { newCohortName = it },
                    onAddClick = {
                        if (newCohortName.isNotBlank()) {
                            cohorts.add(Cohort(UUID.randomUUID().toString(), newCohortName, selectedCourse.id))
                            newCohortName = ""
                        }
                    },
                    selectedItemIndex = filteredCohorts.indexOf(selectedCohort),
                    onItemClick = { index ->
                        val clickedCohort = filteredCohorts[index]
                        if (selectedCohort == clickedCohort) {
                            onCohortSelected(null)
                            onGroupSelected(null)
                        } else {
                            onCohortSelected(clickedCohort)
                            onGroupSelected(null)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            }

            // Groups Section (Filtered by Cohort)
            if (selectedCohort != null) {
                val filteredGroups = groups.filter { it.cohortId == selectedCohort.id }
                ManagementSection(
                    title = "Groups (${selectedCohort.name})",
                    items = filteredGroups.map { it.name },
                    newItemValue = newGroupName,
                    onNewItemValueChange = { newGroupName = it },
                    onAddClick = {
                        if (newGroupName.isNotBlank()) {
                            groups.add(Group(UUID.randomUUID().toString(), newGroupName, selectedCohort.id))
                            newGroupName = ""
                        }
                    },
                    selectedItemIndex = filteredGroups.indexOf(selectedGroup),
                    onItemClick = { index ->
                        val clickedGroup = filteredGroups[index]
                        if (selectedGroup == clickedGroup) {
                            onGroupSelected(null)
                        } else {
                            onGroupSelected(clickedGroup)
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            }

            // Subjects Section (Filtered by Course)
            if (selectedCourse != null) {
                val filteredSubjects = subjects.filter { it.courseId == selectedCourse.id }
                ManagementSection(
                    title = "Subjects (${selectedCourse.name})",
                    items = filteredSubjects.map { it.name },
                    newItemValue = newSubjectName,
                    onNewItemValueChange = { newSubjectName = it },
                    onAddClick = {
                        if (newSubjectName.isNotBlank()) {
                            subjects.add(Subject(UUID.randomUUID().toString(), newSubjectName, selectedCourse.id))
                            newSubjectName = ""
                        }
                    },
                    onItemClick = {}
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.1f))
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ManagementSection(
    title: String,
    items: List<String>,
    newItemValue: String,
    onNewItemValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    selectedItemIndex: Int = -1,
    onItemClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newItemValue,
                onValueChange = onNewItemValueChange,
                label = { Text("Add New", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        items.forEachIndexed { index, item ->
            val isSelected = index == selectedItemIndex
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f)
                ),
                onClick = { onItemClick(index) }
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(12.dp),
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = color.copy(alpha = 0.8f)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}

@Composable
fun GradeBoundaryRow(grade: String, threshold: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = grade,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = threshold,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color.White
            ),
            textAlign = TextAlign.End
        )
    }
}

// Function to calculate dynamic grade based on z-score
fun getDynamicGrade(score: Double, boundaries: GradeBoundaries): String {
    return when {
        score >= boundaries.aThreshold -> "A"
        score >= boundaries.bLowerThreshold -> "B"
        score >= boundaries.cLowerThreshold -> "C"
        score >= boundaries.dLowerThreshold -> "D"
        else -> "F"
    }
}

// Function to get color for grade
fun getGradeColor(grade: String): Color {
    return when (grade) {
        "A" -> Color.Green
        "B" -> Color.Blue
        "C" -> Color(0xFFFFA500) // Orange
        "D" -> Color.Magenta // Purple
        "F" -> Color.Red
        else -> Color.Gray
    }
}

fun seedDatabase(
    universities: MutableList<University>,
    courses: MutableList<Course>,
    cohorts: MutableList<Cohort>,
    groups: MutableList<Group>,
    subjects: MutableList<Subject>,
    students: MutableList<Student>,
    results: MutableList<Result>,
    statsTracker: WelfordStats
) {
    val uniData = listOf(
        "Tech Institute" to listOf("Computer Science", "Software Engineering", "Cybersecurity"),
        "Medical Academy" to listOf("Medicine", "Nursing", "Pharmacy"),
        "Arts College" to listOf("Architecture", "Fine Arts", "Design")
    )

    val subjectData = mapOf(
        "Computer Science" to listOf("Algorithms", "Data Structures", "Operating Systems", "Databases", "AI"),
        "Software Engineering" to listOf("Software Design", "Testing", "Agile", "Mobile Dev", "Cloud Computing"),
        "Cybersecurity" to listOf("Network Security", "Cryptography", "Forensics", "Ethical Hacking", "Security Audit"),
        "Medicine" to listOf("Anatomy", "Physiology", "Biochemistry", "Pathology", "Pharmacology"),
        "Nursing" to listOf("Fundamentals of Nursing", "Community Health", "Mental Health", "Midwifery", "Pediatrics"),
        "Pharmacy" to listOf("Pharmaceutics", "Pharmacology", "Pharmacognosy", "Medicinal Chemistry", "Pharmacy Practice"),
        "Architecture" to listOf("Design Studio", "History of Architecture", "Building Tech", "Urban Planning", "Structural Systems"),
        "Fine Arts" to listOf("Painting", "Sculpture", "Art History", "Drawing", "Printmaking"),
        "Design" to listOf("Graphic Design", "UI/UX", "Typography", "Motion Graphics", "Brand Identity")
    )

    uniData.forEach { (uniName, courseNames) ->
        val uni = University(UUID.randomUUID().toString(), uniName)
        universities.add(uni)

        courseNames.forEach { courseName ->
            val course = Course(UUID.randomUUID().toString(), courseName, uni.id)
            courses.add(course)

            val cohort = Cohort(UUID.randomUUID().toString(), "2023", course.id)
            cohorts.add(cohort)

            val group1 = Group(UUID.randomUUID().toString(), "Group 1", cohort.id)
            val group2 = Group(UUID.randomUUID().toString(), "Group 2", cohort.id)
            groups.addAll(listOf(group1, group2))

            val courseSubjects = subjectData[courseName]?.map {
                Subject(UUID.randomUUID().toString(), it, course.id)
            } ?: emptyList()
            subjects.addAll(courseSubjects)

            listOf(group1, group2).forEach { group ->
                val prefix = when(courseName) {
                    "Computer Science" -> "SCS1"
                    "Software Engineering" -> "SCS2"
                    "Cybersecurity" -> "SCS3"
                    "Medicine" -> "MED1"
                    "Nursing" -> "MED2"
                    "Pharmacy" -> "MED3"
                    "Architecture" -> "ART1"
                    "Fine Arts" -> "ART2"
                    "Design" -> "ART3"
                    else -> "STU"
                }
                
                for (i in 1..30) {
                    val studentId = UUID.randomUUID().toString()
                    val student = Student(
                        id = studentId,
                        name = "Student $i (${uniName.split(" ")[0]} - $courseName)",
                        regNumber = "$prefix/${1000 + i}/2023",
                        groupId = group.id
                    )
                    students.add(student)

                    courseSubjects.forEach { subj ->
                        val score = (40..100).random()
                        statsTracker.addValue(score.toDouble())
                        results.add(Result(studentId, subj.id, score, ""))
                    }
                }
            }
        }
    }

    // After all scores are in statsTracker, update all grades based on final boundaries
    val finalBoundaries = statsTracker.getGradeBoundaries()
    val updatedResults = results.map { it.copy(grade = getDynamicGrade(it.score.toDouble(), finalBoundaries)) }
    results.clear()
    results.addAll(updatedResults)
}