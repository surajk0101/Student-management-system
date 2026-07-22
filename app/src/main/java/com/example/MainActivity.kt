package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaveScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.EduTrackTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.EduViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EduViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EduTrackTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

data class NavItem(
    val tab: AppTab,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainAppScreen(viewModel: EduViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val rollNo by viewModel.rollNo.collectAsStateWithLifecycle()
    val branch by viewModel.branch.collectAsStateWithLifecycle()
    val semester by viewModel.semester.collectAsStateWithLifecycle()
    val overallTarget by viewModel.overallTarget.collectAsStateWithLifecycle()

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val attendanceRecords by viewModel.attendanceRecords.collectAsStateWithLifecycle()
    val leaves by viewModel.leaves.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavItem(AppTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavItem(AppTab.ATTENDANCE, "Attendance", Icons.Filled.Assessment, Icons.Outlined.Assessment),
        NavItem(AppTab.LEAVES, "Leaves", Icons.Filled.EventNote, Icons.Outlined.EventNote),
        NavItem(AppTab.NOTES, "Notes", Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        NavItem(AppTab.TIMETABLE, "Schedule", Icons.Filled.Schedule, Icons.Outlined.Schedule),
        NavItem(AppTab.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_navigation_bar")
            ) {
                navItems.forEach { item ->
                    val isSelected = currentTab == item.tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(item.tab) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontSize = 11.sp
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${item.title.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> HomeScreen(
                    studentName = studentName,
                    rollNo = rollNo,
                    branch = branch,
                    semester = semester,
                    targetPercentage = overallTarget,
                    subjects = subjects,
                    leaves = leaves,
                    notes = notes,
                    schedules = schedules,
                    onNavigateTab = { viewModel.selectTab(it) },
                    onQuickMarkAttendance = { viewModel.selectTab(AppTab.ATTENDANCE) },
                    onQuickApplyLeave = { viewModel.selectTab(AppTab.LEAVES) },
                    onQuickAddNote = { viewModel.selectTab(AppTab.NOTES) }
                )

                AppTab.ATTENDANCE -> AttendanceScreen(
                    subjects = subjects,
                    attendanceRecords = attendanceRecords,
                    onAddSubject = { name, code, teacher, target, color ->
                        viewModel.addSubject(name, code, teacher, target, color)
                    },
                    onLogAttendance = { subject, status, note ->
                        viewModel.logAttendance(subject, status, note)
                    },
                    onDeleteSubject = { viewModel.deleteSubject(it) },
                    onDeleteRecord = { viewModel.deleteAttendanceRecord(it) }
                )

                AppTab.LEAVES -> LeaveScreen(
                    leaves = leaves,
                    onApplyLeave = { type, start, end, reason ->
                        viewModel.applyLeave(type, start, end, reason)
                    },
                    onUpdateStatus = { id, status ->
                        viewModel.updateLeaveStatus(id, status)
                    },
                    onDeleteLeave = { viewModel.deleteLeave(it) }
                )

                AppTab.NOTES -> NotesScreen(
                    notes = notes,
                    subjects = subjects,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                    onAddNote = { title, subject, category, content, isPinned ->
                        viewModel.addNote(title, subject, category, content, isPinned)
                    },
                    onTogglePin = { viewModel.togglePinNote(it) },
                    onDeleteNote = { viewModel.deleteNote(it) }
                )

                AppTab.TIMETABLE -> TimetableScreen(
                    schedules = schedules,
                    subjects = subjects,
                    onAddSchedule = { day, subject, time, room, instructor ->
                        viewModel.addSchedule(day, subject, time, room, instructor)
                    },
                    onDeleteSchedule = { viewModel.deleteSchedule(it) }
                )

                AppTab.PROFILE -> ProfileScreen(
                    studentName = studentName,
                    rollNo = rollNo,
                    branch = branch,
                    semester = semester,
                    targetPercentage = overallTarget,
                    subjectCount = subjects.size,
                    leaveCount = leaves.size,
                    noteCount = notes.size,
                    onUpdateProfile = { name, roll, br, sem, target ->
                        viewModel.updateStudentProfile(name, roll, br, sem, target)
                    }
                )
            }
        }
    }
}
