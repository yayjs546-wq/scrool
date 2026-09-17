package com.example.data

import com.example.model.OfflineVideo
import com.example.model.SleepSession
import com.example.security.EncryptionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class SleepRepository(private val sleepDao: SleepDao) {

    val allSessions: Flow<List<SleepSession>> = sleepDao.getAllSessions()
    val latestSession: Flow<SleepSession?> = sleepDao.getLatestSession()
    val averageEfficiency: Flow<Double?> = sleepDao.getAverageEfficiency()

    suspend fun saveSession(
        efficiency: Int,
        deepSleepMinutes: Int,
        remMinutes: Int,
        lightSleepMinutes: Int,
        awakeMinutes: Int,
        videosScrolled: Int,
        livesSkipped: Int,
        notes: String = "Nighttime auto-scroll wind-down session"
    ): Long {
        val encryptedPayload = EncryptionManager.encrypt(
            "{\"notes\":\"$notes\",\"timestamp\":${System.currentTimeMillis()},\"restingHeartRate\":56,\"hrv\":68}"
        )

        val session = SleepSession(
            startTimeMillis = System.currentTimeMillis() - ((deepSleepMinutes + remMinutes + lightSleepMinutes + awakeMinutes) * 60 * 1000L),
            endTimeMillis = System.currentTimeMillis(),
            efficiencyPercentage = efficiency,
            deepSleepMinutes = deepSleepMinutes,
            remMinutes = remMinutes,
            lightSleepMinutes = lightSleepMinutes,
            awakeMinutes = awakeMinutes,
            videosScrolled = videosScrolled,
            livesSkipped = livesSkipped,
            encryptedHealthPayload = encryptedPayload,
            isSyncedWithAndroidHealth = true
        )
        return sleepDao.insertSession(session)
    }

    suspend fun seedInitialDataIfEmpty() {
        val existing = sleepDao.getAllSessions().firstOrNull()
        if (existing.isNullOrEmpty()) {
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 3600 * 1000L

            val initialSessions = listOf(
                SleepSession(
                    id = 1,
                    startTimeMillis = now - (dayMillis * 1) - (7 * 3600 * 1000L),
                    endTimeMillis = now - (dayMillis * 1),
                    efficiencyPercentage = 94,
                    deepSleepMinutes = 125,
                    remMinutes = 110,
                    lightSleepMinutes = 195,
                    awakeMinutes = 15,
                    videosScrolled = 42,
                    livesSkipped = 9,
                    encryptedHealthPayload = EncryptionManager.encrypt("{\"session\":\"Deep REM sleep\",\"hrv\":74}"),
                    isSyncedWithAndroidHealth = true
                ),
                SleepSession(
                    id = 2,
                    startTimeMillis = now - (dayMillis * 2) - (6 * 3600 * 1000L),
                    endTimeMillis = now - (dayMillis * 2),
                    efficiencyPercentage = 87,
                    deepSleepMinutes = 95,
                    remMinutes = 90,
                    lightSleepMinutes = 170,
                    awakeMinutes = 28,
                    videosScrolled = 58,
                    livesSkipped = 14,
                    encryptedHealthPayload = EncryptionManager.encrypt("{\"session\":\"Wind-down completed with 30m timer\",\"hrv\":65}"),
                    isSyncedWithAndroidHealth = true
                ),
                SleepSession(
                    id = 3,
                    startTimeMillis = now - (dayMillis * 3) - (8 * 3600 * 1000L),
                    endTimeMillis = now - (dayMillis * 3),
                    efficiencyPercentage = 91,
                    deepSleepMinutes = 130,
                    remMinutes = 105,
                    lightSleepMinutes = 210,
                    awakeMinutes = 20,
                    videosScrolled = 30,
                    livesSkipped = 6,
                    encryptedHealthPayload = EncryptionManager.encrypt("{\"session\":\"Smart light amber sunset fade enabled\",\"hrv\":72}"),
                    isSyncedWithAndroidHealth = true
                )
            )

            initialSessions.forEach { sleepDao.insertSession(it) }
        }
    }

    fun getInitialOfflineVideos(): List<OfflineVideo> {
        return listOf(
            OfflineVideo(
                id = "vid_1",
                title = "4K Midnight Rain on Cedar Glass & Soft Thunder",
                creator = "@ZenBinauralSleep",
                category = "Rain ASMR",
                durationSeconds = 12,
                likesCount = "482K",
                soundTrack = "Delta Waves 432Hz Calm Down",
                isLiveStream = false,
                isDownloaded = true,
                downloadProgress = 1.0f,
                fileSizeMb = 3.8,
                gradientColors = listOf(0xFF0F172A, 0xFF1E293B)
            ),
            OfflineVideo(
                id = "vid_2",
                title = "HYPNOTIC Sand Dune Patterns - 100% Loop",
                creator = "@SatisfyingDeepLoops",
                category = "Kinetic Sand",
                durationSeconds = 10,
                likesCount = "891K",
                soundTrack = "Whispering Wind - Ambient Loop",
                isLiveStream = false,
                isDownloaded = true,
                downloadProgress = 1.0f,
                fileSizeMb = 4.2,
                gradientColors = listOf(0xFF2E1065, 0xFF4C1D95)
            ),
            OfflineVideo(
                id = "vid_3",
                title = "[LIVE STREAM] Late Night Chat & Gifts! ⚡",
                creator = "@LoudGamerLive",
                category = "Live Stream",
                durationSeconds = 20,
                likesCount = "2.4K Watching",
                soundTrack = "Loud Microphone Voice & Sub Alerts",
                isLiveStream = true, // TARGET FOR AUTO-SKIP!
                isDownloaded = false,
                downloadProgress = 0.0f,
                fileSizeMb = 0.0,
                gradientColors = listOf(0xFF7F1D1D, 0xFF991B1B)
            ),
            OfflineVideo(
                id = "vid_4",
                title = "Deep Space Andromeda Galaxy Starfield 60fps",
                creator = "@CosmicLullaby",
                category = "Cosmic Ambient",
                durationSeconds = 14,
                likesCount = "620K",
                soundTrack = "Voyager 1 Celestial Drone",
                isLiveStream = false,
                isDownloaded = true,
                downloadProgress = 1.0f,
                fileSizeMb = 5.1,
                gradientColors = listOf(0xFF0C0A3E, 0xFF240046)
            ),
            OfflineVideo(
                id = "vid_5",
                title = "Satisfying Soap Carving Crisp Micro-Sounds",
                creator = "@BedtimeASMR_Slice",
                category = "Bedtime ASMR",
                durationSeconds = 11,
                likesCount = "319K",
                soundTrack = "Crisp Binaural Micro-Taps",
                isLiveStream = false,
                isDownloaded = true,
                downloadProgress = 1.0f,
                fileSizeMb = 3.4,
                gradientColors = listOf(0xFF064E3B, 0xFF065F46)
            ),
            OfflineVideo(
                id = "vid_6",
                title = "Soft Night Train Journey Across Swiss Alps",
                creator = "@CozySleepRides",
                category = "Night Ambiance",
                durationSeconds = 15,
                likesCount = "754K",
                soundTrack = "Rhythmic Tracks & Distant Snowfall",
                isLiveStream = false,
                isDownloaded = true,
                downloadProgress = 1.0f,
                fileSizeMb = 4.7,
                gradientColors = listOf(0xFF1E1B4B, 0xFF312E81)
            )
        )
    }
}
