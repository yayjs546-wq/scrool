package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.SleepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DreamScrollApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var sleepRepository: SleepRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getDatabase(this)
        sleepRepository = SleepRepository(database.sleepDao())

        // Seed baseline records
        CoroutineScope(Dispatchers.IO).launch {
            sleepRepository.seedInitialDataIfEmpty()
        }

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Soft Haptic Wake Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Gentle morning wake-up notification with soft haptic crescendo"
                enableVibration(true)
            }

            val sleepTimerChannel = NotificationChannel(
                TIMER_CHANNEL_ID,
                "Sleep Timer & Fade",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sleep timer countdown and battery saving notices"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(alarmChannel)
            manager.createNotificationChannel(sleepTimerChannel)
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "dreamscroll_wake_alarm_channel"
        const val TIMER_CHANNEL_ID = "dreamscroll_sleep_timer_channel"

        lateinit var instance: DreamScrollApp
            private set
    }
}
