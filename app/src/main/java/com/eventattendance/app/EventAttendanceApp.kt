package com.eventattendance.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EventAttendanceApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
