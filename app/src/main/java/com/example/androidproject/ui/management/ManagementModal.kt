package com.example.androidproject.ui.management

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.androidproject.model.Cohort
import com.example.androidproject.model.Course
import com.example.androidproject.model.Group
import com.example.androidproject.model.Subject
import com.example.androidproject.model.University
import java.util.UUID

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
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
                label = { Text("Add New") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        items.forEachIndexed { index, item ->
            val isSelected = index == selectedItemIndex
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                onClick = { onItemClick(index) }
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun StatisticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GradeBoundaryRow(grade: String, threshold: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = grade,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = threshold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}
