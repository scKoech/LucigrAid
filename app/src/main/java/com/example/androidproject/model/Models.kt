package com.example.androidproject.model

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
