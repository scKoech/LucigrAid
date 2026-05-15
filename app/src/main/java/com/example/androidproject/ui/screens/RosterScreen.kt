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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidproject.model.GradeBoundaries
import com.example.androidproject.model.Group
import com.example.androidproject.model.Result
import com.example.androidproject.model.Student
import com.example.androidproject.model.StudentResult
import com.example.androidproject.util.getDynamicGrade
import com.example.androidproject.util.getGradeColor

@Composable
fun RosterScreen(
    students: List<Student>,
    results: List<Result>,
    selectedGroup: Group?,
    gradeBoundaries: GradeBoundaries?
) {
    // State for showing all students
    var showAllStudents by remember { mutableStateOf(false) }

    // State for search
    var searchName by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<List<StudentResult>>(emptyList()) }
    var showSearchResult by remember { mutableStateOf(false) }
    var searchSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showSearchSuggestions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
                            .filter { it.groupId == selectedGroup.id }
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
                                        val grade = if (gradeBoundaries != null) getDynamicGrade(avg, gradeBoundaries) else "F"
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
                    val grade = if (gradeBoundaries != null) getDynamicGrade(avg, gradeBoundaries) else "F"
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

        Spacer(modifier = Modifier.height(32.dp))

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
            students.filter { it.groupId == selectedGroup.id }
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
                                    val grade = if (gradeBoundaries != null) getDynamicGrade(avg, gradeBoundaries) else "F"
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

        Spacer(modifier = Modifier.height(48.dp))
    }
}
