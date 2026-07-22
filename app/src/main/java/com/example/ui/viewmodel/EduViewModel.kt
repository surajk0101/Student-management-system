package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.EduDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.LeaveApplication
import com.example.data.model.ScheduleSlot
import com.example.data.model.StudyNote
import com.example.data.model.Subject
import com.example.data.repository.EduRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab {
    HOME, ATTENDANCE, LEAVES, NOTES, TIMETABLE, PROFILE
}

class EduViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: EduRepository

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Student Profile State
    private val _studentName = MutableStateFlow("Alex Rivera")
    val studentName: StateFlow<String> = _studentName.asStateFlow()

    private val _rollNo = MutableStateFlow("2024-CS-108")
    val rollNo: StateFlow<String> = _rollNo.asStateFlow()

    private val _branch = MutableStateFlow("Computer Science & Engineering")
    val branch: StateFlow<String> = _branch.asStateFlow()

    private val _semester = MutableStateFlow("Semester 5")
    val semester: StateFlow<String> = _semester.asStateFlow()

    private val _overallTarget = MutableStateFlow(75.0)
    val overallTarget: StateFlow<Double> = _overallTarget.asStateFlow()

    init {
        val database = EduDatabase.getDatabase(application)
        repository = EduRepository(database)

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val subjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaves: StateFlow<List<LeaveApplication>> = repository.allLeaves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<StudyNote>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<ScheduleSlot>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateStudentProfile(name: String, roll: String, br: String, sem: String, target: Double) {
        _studentName.value = name
        _rollNo.value = roll
        _branch.value = br
        _semester.value = sem
        _overallTarget.value = target
    }

    // Actions
    fun addSubject(name: String, code: String, teacher: String, target: Double, colorHex: String) {
        viewModelScope.launch {
            val subject = Subject(
                name = name,
                code = code,
                teacherName = teacher,
                targetPercentage = target,
                presentClasses = 0,
                totalClasses = 0,
                colorHex = colorHex
            )
            repository.addSubject(subject)
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun logAttendance(subject: Subject, status: String, note: String = "") {
        viewModelScope.launch {
            repository.logAttendance(subject, status, note)
        }
    }

    fun deleteAttendanceRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteAttendanceRecord(record)
        }
    }

    fun applyLeave(leaveType: String, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            val leave = LeaveApplication(
                studentName = _studentName.value,
                rollNo = _rollNo.value,
                leaveType = leaveType,
                startDate = startDate,
                endDate = endDate,
                reason = reason,
                status = "PENDING"
            )
            repository.applyLeave(leave)
        }
    }

    fun updateLeaveStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(id, status)
        }
    }

    fun deleteLeave(leave: LeaveApplication) {
        viewModelScope.launch {
            repository.deleteLeave(leave)
        }
    }

    fun addNote(title: String, subjectName: String, category: String, content: String, isPinned: Boolean) {
        viewModelScope.launch {
            val note = StudyNote(
                title = title,
                subjectName = subjectName,
                category = category,
                content = content,
                isPinned = isPinned
            )
            repository.addNote(note)
        }
    }

    fun togglePinNote(note: StudyNote) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun deleteNote(note: StudyNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addSchedule(dayOfWeek: Int, subjectName: String, timeSlot: String, roomNumber: String, instructor: String) {
        viewModelScope.launch {
            val slot = ScheduleSlot(
                dayOfWeek = dayOfWeek,
                subjectName = subjectName,
                timeSlot = timeSlot,
                roomNumber = roomNumber,
                instructor = instructor
            )
            repository.addSchedule(slot)
        }
    }

    fun deleteSchedule(schedule: ScheduleSlot) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
}
