package com.example.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SmartLightingState(
    val isEnabled: Boolean = true,
    val brand: String = "Philips Hue / Matter",
    val bridgeStatus: String = "Connected (Bedroom Lamp & Ambient Strip)",
    val targetFadeMinutes: Int = 15,
    val currentBrightness: Float = 1.0f, // 0.0f to 1.0f
    val colorTemperatureKelvin: Int = 2200, // Sunset amber candlelight
    val isFadingActive: Boolean = false,
    val remainingFadeSeconds: Int = 0
)

class SmartLightingManager {

    private val _lightingState = MutableStateFlow(SmartLightingState())
    val lightingState: StateFlow<SmartLightingState> = _lightingState.asStateFlow()

    private var fadeJob: Job? = null

    fun toggleEnabled(enabled: Boolean) {
        _lightingState.value = _lightingState.value.copy(isEnabled = enabled)
    }

    fun setFadeDuration(minutes: Int) {
        _lightingState.value = _lightingState.value.copy(targetFadeMinutes = minutes)
    }

    fun setManualBrightness(brightness: Float) {
        _lightingState.value = _lightingState.value.copy(currentBrightness = brightness.coerceIn(0f, 1f))
    }

    fun setColorTemperature(kelvin: Int) {
        _lightingState.value = _lightingState.value.copy(colorTemperatureKelvin = kelvin)
    }

    /**
     * Initiates the gentle exponential fade-out curve matching bedtime circadian biology.
     */
    fun startGentleFade(scope: CoroutineScope, durationMinutes: Int = _lightingState.value.targetFadeMinutes) {
        cancelFade()
        val totalSeconds = durationMinutes * 60
        _lightingState.value = _lightingState.value.copy(
            isFadingActive = true,
            remainingFadeSeconds = totalSeconds,
            currentBrightness = 1.0f
        )

        fadeJob = scope.launch(Dispatchers.Default) {
            val initialBrightness = _lightingState.value.currentBrightness
            val startKelvin = 3000
            val targetKelvin = 2000 // Sunset candlelight

            for (sec in 0..totalSeconds) {
                if (!isActive) break
                val progress = sec.toFloat() / totalSeconds
                // Ease-in exponential curve for calm transition
                val factor = (1f - progress).coerceIn(0f, 1f)
                val newBrightness = initialBrightness * (factor * factor)
                val newKelvin = (startKelvin - ((startKelvin - targetKelvin) * progress)).toInt()

                _lightingState.value = _lightingState.value.copy(
                    currentBrightness = newBrightness,
                    colorTemperatureKelvin = newKelvin,
                    remainingFadeSeconds = (totalSeconds - sec)
                )

                delay(1000)
            }

            _lightingState.value = _lightingState.value.copy(
                isFadingActive = false,
                currentBrightness = 0f,
                remainingFadeSeconds = 0
            )
        }
    }

    fun cancelFade() {
        fadeJob?.cancel()
        fadeJob = null
        _lightingState.value = _lightingState.value.copy(
            isFadingActive = false,
            remainingFadeSeconds = 0
        )
    }
}
