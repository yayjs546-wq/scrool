package com.example.service

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class SleepSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isTracking = false
    
    // Store movement intensity over time to determine sleep stages
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f
    
    private var totalMovementEvents = 0
    private var heavyMovementEvents = 0
    private var lightMovementEvents = 0
    private var noMovementEvents = 0
    
    private var lastEventTime = 0L

    private val _currentMovementLevel = MutableStateFlow(0f)
    val currentMovementLevel: StateFlow<Float> = _currentMovementLevel.asStateFlow()

    fun startTracking() {
        if (isTracking || accelerometer == null) return
        
        totalMovementEvents = 0
        heavyMovementEvents = 0
        lightMovementEvents = 0
        noMovementEvents = 0
        
        isTracking = true
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        sensorManager.unregisterListener(this)
    }

    fun generateSessionStats(durationMinutes: Int): SleepStats {
        if (durationMinutes <= 0) {
            return SleepStats(0, 0, 0, 0, 0)
        }
        
        // If we didn't track long enough to have meaningful data, fallback to real-time proportionality
        val totalRecordedEvents = heavyMovementEvents + lightMovementEvents + noMovementEvents
        if (totalRecordedEvents == 0) {
            return SleepStats(
                efficiency = 0,
                deep = 0,
                rem = 0,
                light = durationMinutes, // assume all light sleep if no data
                awake = 0
            )
        }
        
        val heavyPct = heavyMovementEvents.toFloat() / totalRecordedEvents
        val lightPct = lightMovementEvents.toFloat() / totalRecordedEvents
        val noPct = noMovementEvents.toFloat() / totalRecordedEvents

        // Map movement to stages:
        // Heavy movement -> Awake
        // Light movement -> REM / Light
        // No movement -> Deep
        
        val awakeMins = (durationMinutes * heavyPct).toInt()
        val deepMins = (durationMinutes * noPct).toInt()
        
        // Split the remaining light movement between Light and REM
        val remainingMins = durationMinutes - awakeMins - deepMins
        val remMins = (remainingMins * 0.4f).toInt()
        val lightMins = remainingMins - remMins
        
        val efficiency = if (durationMinutes > 0) {
            ((durationMinutes - awakeMins).toFloat() / durationMinutes * 100).toInt()
        } else 100
        
        return SleepStats(
            efficiency = efficiency.coerceIn(0, 100),
            deep = deepMins,
            rem = remMins,
            light = lightMins,
            awake = awakeMins
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isTracking) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEventTime > 2000) { // Sample every 2 seconds to save battery
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            if (lastEventTime != 0L) {
                val deltaX = Math.abs(lastX - x)
                val deltaY = Math.abs(lastY - y)
                val deltaZ = Math.abs(lastZ - z)

                // Calculate vector magnitude of movement
                val movement = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()
                
                _currentMovementLevel.value = movement

                when {
                    movement > 2.5f -> heavyMovementEvents++
                    movement > 0.5f -> lightMovementEvents++
                    else -> noMovementEvents++
                }
                totalMovementEvents++
            }

            lastX = x
            lastY = y
            lastZ = z
            lastEventTime = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed
    }
}

data class SleepStats(
    val efficiency: Int,
    val deep: Int,
    val rem: Int,
    val light: Int,
    val awake: Int
)
