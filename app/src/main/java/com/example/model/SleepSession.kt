package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracks nightly sleep session metrics, auto-scroll stats, and encrypted health data.
 */
@Entity(tableName = "sleep_sessions")
data class SleepSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTimeMillis: Long = System.currentTimeMillis() - (7 * 3600 * 1000),
    val endTimeMillis: Long = System.currentTimeMillis(),
    val efficiencyPercentage: Int = 88,
    val deepSleepMinutes: Int = 110,
    val remMinutes: Int = 95,
    val lightSleepMinutes: Int = 180,
    val awakeMinutes: Int = 25,
    val videosScrolled: Int = 34,
    val livesSkipped: Int = 7,
    val encryptedHealthPayload: String = "",
    val isSyncedWithAndroidHealth: Boolean = true
) {
    val totalDurationMinutes: Int
        get() = deepSleepMinutes + remMinutes + lightSleepMinutes + awakeMinutes

    val durationFormatted: String
        get() {
            val hours = totalDurationMinutes / 60
            val mins = totalDurationMinutes % 60
            return "${hours}h ${mins}m"
        }
}
