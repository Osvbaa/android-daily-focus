package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dailyfocus.core.database.model.ReminderSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderSettingsDao {
    @Query("SELECT * FROM task_reminder_settings WHERE id = 0")
    fun observe(): Flow<ReminderSettingsEntity?>

    @Query("SELECT * FROM task_reminder_settings WHERE id = 0")
    suspend fun get(): ReminderSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: ReminderSettingsEntity)
}
