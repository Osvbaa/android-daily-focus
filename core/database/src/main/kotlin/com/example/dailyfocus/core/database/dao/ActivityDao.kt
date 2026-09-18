package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.dailyfocus.core.database.model.ActivityEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activity_events ORDER BY epoch_day ASC, occurred_at_millis ASC")
    fun observeAll(): Flow<List<ActivityEventEntity>>

    @Query("SELECT * FROM activity_events WHERE idempotency_key = :key")
    suspend fun get(key: String): ActivityEventEntity?

    @Query("SELECT COUNT(*) FROM activity_events WHERE category = :category AND epoch_day = :epochDay")
    suspend fun countCategoryOnDay(category: String, epochDay: Long): Int

    @Query("SELECT COALESCE(SUM(points), 0) FROM activity_events WHERE epoch_day = :epochDay")
    suspend fun pointsOnDay(epochDay: Long): Int

    @Query("SELECT COALESCE(SUM(points), 0) FROM activity_events")
    suspend fun totalPoints(): Int

    @Upsert
    suspend fun upsert(event: ActivityEventEntity)
}
