package com.example.model

/**
 * Model for bedtime short-form videos supporting auto-scrolling,
 * instant live-stream skipping, and offline downloading.
 */
data class OfflineVideo(
    val id: String,
    val title: String,
    val creator: String,
    val category: String,
    val durationSeconds: Int = 15,
    val likesCount: String = "124K",
    val soundTrack: String = "Original Sound - Rain & Binaural Theta",
    val isLiveStream: Boolean = false,
    val isDownloaded: Boolean = true,
    val downloadProgress: Float = 1.0f,
    val fileSizeMb: Double = 4.2,
    val gradientColors: List<Long> = listOf(0xFF1E1B4B, 0xFF311042)
)
