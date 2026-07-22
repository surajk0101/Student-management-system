package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val code: String,
    val teacherName: String,
    val targetPercentage: Double = 75.0,
    val presentClasses: Int = 0,
    val totalClasses: Int = 0,
    val colorHex: String = "#3F51B5"
) {
    val currentPercentage: Double
        get() = if (totalClasses == 0) 0.0 else (presentClasses.toDouble() / totalClasses) * 100.0

    fun calculateMargin(): String {
        if (totalClasses == 0) return "No classes held yet"
        val current = currentPercentage
        val target = targetPercentage
        if (current >= target) {
            // How many classes can be missed without dropping below target
            // (present) / (total + x) >= target/100  => present * 100 / target >= total + x
            val maxTotal = (presentClasses * 100.0 / target).toInt()
            val canBunk = maxTotal - totalClasses
            return if (canBunk > 0) "You can miss up to $canBunk class(es)" else "On track (Don't miss the next class!)"
        } else {
            // How many consecutive classes to attend to reach target
            // (present + x) / (total + x) >= target/100
            // present * 100 + 100x >= target * total + target * x
            // x * (100 - target) >= target * total - present * 100
            val numNeeded = Math.ceil((target * totalClasses - presentClasses * 100.0) / (100.0 - target)).toInt()
            return "Must attend next $numNeeded class(es) to reach ${target.toInt()}%"
        }
    }
}

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subjectId: Long,
    val subjectName: String,
    val timestamp: Long,
    val status: String, // PRESENT, ABSENT, CANCELLED
    val note: String = ""
)

@Entity(tableName = "leave_applications")
data class LeaveApplication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentName: String = "Alex Rivera",
    val rollNo: String = "2024-CS-108",
    val leaveType: String, // Sick Leave, Duty Leave, Personal, Urgent
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val appliedOn: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_notes")
data class StudyNote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subjectName: String,
    val category: String, // Lecture Notes, Exam Prep, Assignment, Reference
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "schedule_slots")
data class ScheduleSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dayOfWeek: Int, // 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
    val subjectName: String,
    val timeSlot: String, // e.g. "09:00 AM - 10:00 AM"
    val roomNumber: String,
    val instructor: String
)
