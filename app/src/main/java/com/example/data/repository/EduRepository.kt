package com.example.data.repository

import com.example.data.db.EduDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.LeaveApplication
import com.example.data.model.ScheduleSlot
import com.example.data.model.StudyNote
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

class EduRepository(private val database: EduDatabase) {

    private val subjectDao = database.subjectDao()
    private val attendanceDao = database.attendanceDao()
    private val leaveDao = database.leaveDao()
    private val noteDao = database.noteDao()
    private val scheduleDao = database.scheduleDao()

    // Subjects & Attendance
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()

    suspend fun addSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)

    suspend fun logAttendance(subject: Subject, status: String, note: String = "") {
        val record = AttendanceRecord(
            subjectId = subject.id,
            subjectName = subject.name,
            timestamp = System.currentTimeMillis(),
            status = status,
            note = note
        )
        attendanceDao.insertRecord(record)

        val updatedPresent = if (status == "PRESENT") subject.presentClasses + 1 else subject.presentClasses
        val updatedTotal = if (status != "CANCELLED") subject.totalClasses + 1 else subject.totalClasses

        val updatedSubject = subject.copy(
            presentClasses = updatedPresent,
            totalClasses = updatedTotal
        )
        subjectDao.updateSubject(updatedSubject)
    }

    suspend fun deleteAttendanceRecord(record: AttendanceRecord) {
        attendanceDao.deleteRecord(record)
    }

    // Leaves
    val allLeaves: Flow<List<LeaveApplication>> = leaveDao.getAllLeaves()

    suspend fun applyLeave(leave: LeaveApplication): Long = leaveDao.insertLeave(leave)
    suspend fun updateLeaveStatus(id: Long, status: String) = leaveDao.updateLeaveStatus(id, status)
    suspend fun deleteLeave(leave: LeaveApplication) = leaveDao.deleteLeave(leave)

    // Notes
    val allNotes: Flow<List<StudyNote>> = noteDao.getAllNotes()

    suspend fun addNote(note: StudyNote): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: StudyNote) = noteDao.updateNote(note)
    suspend fun deleteNote(note: StudyNote) = noteDao.deleteNote(note)

    // Schedules
    fun getScheduleForDay(day: Int): Flow<List<ScheduleSlot>> = scheduleDao.getScheduleForDay(day)
    val allSchedules: Flow<List<ScheduleSlot>> = scheduleDao.getAllSchedules()

    suspend fun addSchedule(schedule: ScheduleSlot): Long = scheduleDao.insertSchedule(schedule)
    suspend fun deleteSchedule(schedule: ScheduleSlot) = scheduleDao.deleteSchedule(schedule)

    // Initial Data Seeding
    suspend fun seedInitialDataIfEmpty() {
        if (subjectDao.getSubjectCount() == 0) {
            val cs101 = Subject(
                name = "Computer Networks",
                code = "CS-301",
                teacherName = "Dr. Robert Vance",
                targetPercentage = 75.0,
                presentClasses = 22,
                totalClasses = 26,
                colorHex = "#3F51B5"
            )
            val cs102 = Subject(
                name = "Data Structures & Algo",
                code = "CS-201",
                teacherName = "Prof. Elena Rostova",
                targetPercentage = 80.0,
                presentClasses = 28,
                totalClasses = 32,
                colorHex = "#009688"
            )
            val math = Subject(
                name = "Applied Mathematics III",
                code = "MA-202",
                teacherName = "Dr. Alan Turing",
                targetPercentage = 75.0,
                presentClasses = 14,
                totalClasses = 20,
                colorHex = "#E91E63"
            )
            val physics = Subject(
                name = "Quantum Physics",
                code = "PH-105",
                teacherName = "Dr. Marie Curie",
                targetPercentage = 75.0,
                presentClasses = 18,
                totalClasses = 20,
                colorHex = "#FF9800"
            )

            val cs101Id = subjectDao.insertSubject(cs101)
            val cs102Id = subjectDao.insertSubject(cs102)
            val mathId = subjectDao.insertSubject(math)
            val physId = subjectDao.insertSubject(physics)

            val now = System.currentTimeMillis()
            val dayInMillis = 86400000L

            attendanceDao.insertRecord(AttendanceRecord(subjectId = cs101Id, subjectName = "Computer Networks", timestamp = now - dayInMillis, status = "PRESENT", note = "TCP/IP Handshake Lecture"))
            attendanceDao.insertRecord(AttendanceRecord(subjectId = mathId, subjectName = "Applied Mathematics III", timestamp = now - 2 * dayInMillis, status = "ABSENT", note = "Fever"))
            attendanceDao.insertRecord(AttendanceRecord(subjectId = cs102Id, subjectName = "Data Structures & Algo", timestamp = now - 3 * dayInMillis, status = "PRESENT", note = "AVL Trees Lab"))

            // Sample Leave Applications
            leaveDao.insertLeave(
                LeaveApplication(
                    studentName = "Alex Rivera",
                    rollNo = "2024-CS-108",
                    leaveType = "Sick Leave",
                    startDate = "2026-07-25",
                    endDate = "2026-07-26",
                    reason = "Viral fever and doctor advised 2 days bed rest.",
                    status = "APPROVED",
                    appliedOn = now - 4 * dayInMillis
                )
            )
            leaveDao.insertLeave(
                LeaveApplication(
                    studentName = "Alex Rivera",
                    rollNo = "2024-CS-108",
                    leaveType = "Duty Leave",
                    startDate = "2026-07-30",
                    endDate = "2026-07-30",
                    reason = "Representing college at Inter-University Hackathon.",
                    status = "PENDING",
                    appliedOn = now - dayInMillis
                )
            )

            // Sample Notes
            noteDao.insertNote(
                StudyNote(
                    title = "OS Process Scheduling Summary",
                    subjectName = "Computer Networks",
                    category = "Lecture Notes",
                    content = "Round Robin algorithm uses time quanta (typically 10-100ms). Preemptive nature ensures fair share of CPU time among process queues.",
                    isPinned = true
                )
            )
            noteDao.insertNote(
                StudyNote(
                    title = "Fourier Series Formulas & Properties",
                    subjectName = "Applied Mathematics III",
                    category = "Exam Prep",
                    content = "Euler's formulas for a0, an, bn coefficients over period [-L, L]. Even functions have only cosine terms, odd functions have only sine terms.",
                    isPinned = false
                )
            )

            // Sample Schedule
            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 1, subjectName = "Computer Networks", timeSlot = "09:00 AM - 10:00 AM", roomNumber = "Lab 302", instructor = "Dr. Robert Vance"))
            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 1, subjectName = "Data Structures", timeSlot = "10:15 AM - 11:15 AM", roomNumber = "Hall B", instructor = "Prof. Elena Rostova"))
            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 1, subjectName = "Applied Mathematics III", timeSlot = "11:30 AM - 12:30 PM", roomNumber = "Room 105", instructor = "Dr. Alan Turing"))

            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 2, subjectName = "Quantum Physics", timeSlot = "09:00 AM - 10:30 AM", roomNumber = "Lab 101", instructor = "Dr. Marie Curie"))
            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 2, subjectName = "Data Structures Lab", timeSlot = "11:00 AM - 01:00 PM", roomNumber = "CS Lab 1", instructor = "Prof. Elena Rostova"))

            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 3, subjectName = "Computer Networks", timeSlot = "09:00 AM - 10:00 AM", roomNumber = "Lab 302", instructor = "Dr. Robert Vance"))
            scheduleDao.insertSchedule(ScheduleSlot(dayOfWeek = 3, subjectName = "Applied Mathematics III", timeSlot = "10:15 AM - 11:15 AM", roomNumber = "Room 105", instructor = "Dr. Alan Turing"))
        }
    }
}
