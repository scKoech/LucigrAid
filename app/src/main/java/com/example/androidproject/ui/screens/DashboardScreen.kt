package com.example.androidproject.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.example.androidproject.model.Cohort
import com.example.androidproject.model.Course
import com.example.androidproject.model.GradeBoundaries
import com.example.androidproject.model.Group
import com.example.androidproject.model.University
import com.example.androidproject.ui.management.GradeBoundaryRow
import com.example.androidproject.ui.management.StatisticRow
import com.example.androidproject.util.getGradeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    universities: List<University>,
    courses: List<Course>,
    cohorts: List<Cohort>,
    groups: List<Group>,
    selectedUni: University?,
    selectedCourse: Course?,
    selectedCohort: Cohort?,
    selectedGroup: Group?,
    onUniSelected: (University?) -> Unit,
    onCourseSelected: (Course?) -> Unit,
    onCohortSelected: (Cohort?) -> Unit,
    onGroupSelected: (Group?) -> Unit,
    gradeBoundaries: GradeBoundaries?,
    studentCount: Int
) {
    var courseExpanded by remember { mutableStateOf(false) }
    var groupExpanded by remember { mutableStateOf(false) }

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
            text = "Dashboard",
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
                                onCourseSelected(course)
                                onCohortSelected(null)
                                onGroupSelected(null)
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
                                onGroupSelected(group)
                                groupExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Dynamic Grade Distribution Panel
        if (gradeBoundaries != null && studentCount > 0) {
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
                    StatisticRow(
                        label = "Mean (μ)",
                        value = String.format("%.2f", gradeBoundaries.mean)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatisticRow(
                        label = "Std Dev (σ)",
                        value = String.format("%.2f", gradeBoundaries.stdDev)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatisticRow(
                        label = "Students",
                        value = studentCount.toString()
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

                    GradeBoundaryRow(
                        grade = "A",
                        threshold = "≥ ${String.format("%.2f", gradeBoundaries.aThreshold)}",
                        color = getGradeColor("A")
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GradeBoundaryRow(
                        grade = "B",
                        threshold = "${String.format("%.2f", gradeBoundaries.bLowerThreshold)} - ${String.format("%.2f", gradeBoundaries.bUpperThreshold)}",
                        color = getGradeColor("B")
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GradeBoundaryRow(
                        grade = "C",
                        threshold = "${String.format("%.2f", gradeBoundaries.cLowerThreshold)} - ${String.format("%.2f", gradeBoundaries.cUpperThreshold)}",
                        color = getGradeColor("C")
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GradeBoundaryRow(
                        grade = "D",
                        threshold = "${String.format("%.2f", gradeBoundaries.dLowerThreshold)} - ${String.format("%.2f", gradeBoundaries.dUpperThreshold)}",
                        color = getGradeColor("D")
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    GradeBoundaryRow(
                        grade = "F",
                        threshold = "< ${String.format("%.2f", gradeBoundaries.dLowerThreshold)}",
                        color = getGradeColor("F")
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
