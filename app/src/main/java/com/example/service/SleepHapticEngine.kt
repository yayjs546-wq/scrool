package com.example.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Provides gentle, soft haptic alarm crescendo and dopamine-fuelled micro-interactions.
 */
class SleepHapticEngine(private val context: Context) {

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var alarmHapticJob: Job? = null

    /**
     * Soft haptic alarm notification:
     * Starts with an ultra-gentle, whisper-soft rhythmic sine pulse,
     * gradually escalating over 30 seconds to wake the user peacefully without jarring shock.
     */
    fun startSoftHapticAlarm(scope: CoroutineScope, onStop: () -> Unit = {}) {
        stopHapticAlarm()
        alarmHapticJob = scope.launch(Dispatchers.Default) {
            try {
                // Crescendo progression: starts at 15% amplitude, scales to 80%
                val pulseIntervals = listOf(
                    Pair(12, 100L), // ultra soft
                    Pair(25, 120L),
                    Pair(45, 150L),
                    Pair(65, 180L),
                    Pair(90, 220L),
                    Pair(128, 250L)
                )

                for ((amplitude, durationMs) in pulseIntervals) {
                    if (!isActive) break
                    for (i in 0 until 4) {
                        if (!isActive) break
                        playGentlePulse(durationMs, amplitude)
                        delay(600)
                    }
                    delay(800)
                }

                // Continuous gentle rhythm until dismissed
                while (isActive) {
                    playGentlePulse(200, 140)
                    delay(700)
                    playGentlePulse(150, 100)
                    delay(1200)
                }
            } finally {
                vibrator.cancel()
                onStop()
            }
        }
    }

    fun stopHapticAlarm() {
        alarmHapticJob?.cancel()
        alarmHapticJob = null
        try {
            vibrator.cancel()
        } catch (_: Exception) {}
    }

    private fun playGentlePulse(durationMs: Long, amplitude: Int) {
        if (!vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val clampedAmp = amplitude.coerceIn(1, 255)
                val effect = VibrationEffect.createOneShot(durationMs, clampedAmp)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    /**
     * Tactile dopamine click for interactive buttons and switches.
     */
    fun triggerDopamineClick() {
        playGentlePulse(22, 90)
    }

    /**
     * Satisfying double-pop haptic when auto-skipping a live stream.
     */
    fun triggerLiveSkippedHaptic() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val timings = longArrayOf(0, 30, 60, 45)
                val amplitudes = intArrayOf(0, 120, 0, 180)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } catch (_: Exception) {
                playGentlePulse(45, 150)
            }
        } else {
            playGentlePulse(45, 150)
        }
    }

    /**
     * Celebratory ripple when completing a sleep goal or setting sleep timer.
     */
    fun triggerSuccessRipple() {
        playGentlePulse(70, 160)
    }
}
