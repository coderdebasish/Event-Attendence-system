package com.eventattendance.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eventattendance.app.data.model.AttendanceRecord
import com.eventattendance.app.data.model.Event
import com.eventattendance.app.data.model.EventStudent

@Database(
    entities = [
        Event::class,
        EventStudent::class,
        AttendanceRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
}
