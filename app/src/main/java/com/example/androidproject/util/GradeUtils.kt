package com.example.androidproject.util

import androidx.compose.ui.graphics.Color
import com.example.androidproject.model.GradeBoundaries

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

// Function to get color for grade (using Material-like colors but hardcoded for now, could be dynamic)
fun getGradeColor(grade: String): Color {
    return when (grade) {
        "A" -> Color(0xFF4CAF50) // Green
        "B" -> Color(0xFF2196F3) // Blue
        "C" -> Color(0xFFFF9800) // Orange
        "D" -> Color(0xFF9C27B0) // Purple
        "F" -> Color(0xFFF44336) // Red
        else -> Color.Gray
    }
}
