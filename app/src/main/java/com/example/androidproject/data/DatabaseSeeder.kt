package com.example.androidproject.data

import com.example.androidproject.model.Cohort
import com.example.androidproject.model.Course
import com.example.androidproject.model.Group
import com.example.androidproject.model.Result
import com.example.androidproject.model.Student
import com.example.androidproject.model.Subject
import com.example.androidproject.model.University
import com.example.androidproject.model.WelfordStats
import com.example.androidproject.util.getDynamicGrade
import java.util.UUID

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
