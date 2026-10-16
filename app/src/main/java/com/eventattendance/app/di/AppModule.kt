package com.eventattendance.app.di

import android.content.Context
import androidx.room.Room
import com.eventattendance.app.core.audio.SoundFeedbackManager
import com.eventattendance.app.data.local.*
import com.eventattendance.app.data.remote.SupabaseAttendanceApi
import com.eventattendance.app.data.remote.SupabaseClientProvider
import com.eventattendance.app.data.remote.SupabaseRealtimeManager
import com.eventattendance.app.data.repository.*
import com.eventattendance.app.domain.usecase.SearchStudentsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "event_attendance.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideEventDao(database: AppDatabase): EventDao = database.eventDao()

    @Provides
    fun provideStudentDao(database: AppDatabase): StudentDao = database.studentDao()

    @Provides
    fun provideAttendanceDao(database: AppDatabase): AttendanceDao = database.attendanceDao()

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        studentDao: StudentDao,
        attendanceDao: AttendanceDao,
        api: SupabaseAttendanceApi
    ): AttendanceRepository {
        return AttendanceRepositoryImpl(studentDao, attendanceDao, api)
    }

    @Provides
    @Singleton
    fun provideEventRepository(
        eventDao: EventDao,
        api: SupabaseAttendanceApi
    ): EventRepository {
        return EventRepositoryImpl(eventDao, api)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        clientProvider: SupabaseClientProvider,
        preferences: UserPreferences
    ): AuthRepository {
        return AuthRepositoryImpl(clientProvider, preferences)
    }

    @Provides
    @Singleton
    fun provideSearchStudentsUseCase(): SearchStudentsUseCase {
        return SearchStudentsUseCase()
    }
}
