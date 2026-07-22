package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.LeaveApplication
import com.example.data.model.ScheduleSlot
import com.example.data.model.StudyNote
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Long): Subject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectCount(): Int
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_applications ORDER BY appliedOn DESC")
    fun getAllLeaves(): Flow<List<LeaveApplication>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveApplication): Long

    @Query("UPDATE leave_applications SET status = :status WHERE id = :id")
    suspend fun updateLeaveStatus(id: Long, status: String)

    @Delete
    suspend fun deleteLeave(leave: LeaveApplication)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM study_notes ORDER BY isPinned DESC, createdAt DESC")
    fun getAllNotes(): Flow<List<StudyNote>>

    @Query("SELECT * FROM study_notes WHERE subjectName = :subject ORDER BY isPinned DESC, createdAt DESC")
    fun getNotesForSubject(subject: String): Flow<List<StudyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNote): Long

    @Update
    suspend fun updateNote(note: StudyNote)

    @Delete
    suspend fun deleteNote(note: StudyNote)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_slots WHERE dayOfWeek = :day ORDER BY timeSlot ASC")
    fun getScheduleForDay(day: Int): Flow<List<ScheduleSlot>>

    @Query("SELECT * FROM schedule_slots ORDER BY dayOfWeek ASC, timeSlot ASC")
    fun getAllSchedules(): Flow<List<ScheduleSlot>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleSlot): Long

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleSlot)
}
