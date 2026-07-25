package com.msam.ringkesin

import android.app.Application
import com.msam.ringkesin.data.local.AppDatabase
import com.msam.ringkesin.service.RecordingManager

class RingkesinApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var recordingManager: RecordingManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        recordingManager = RecordingManager(this)
    }
}
