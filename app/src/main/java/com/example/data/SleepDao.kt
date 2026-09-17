package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.SleepSession
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {

    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC")
    fun getAllSessions(): Flow<List<SleepSession>>

    @Query("SELECT * FROM sleep_sessions ORDER BY endTimeMillis DESC LIMIT 1")
    fun getLatestSession(): Flow<SleepSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SleepSession): Long

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("SELECT AVG(efficiencyPercentage) FROM sleep_sessions")
    fun getAverageEfficiency(): Flow<Double?>
}
