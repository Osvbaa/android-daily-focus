package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.dailyfocus.core.database.model.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun get(id: String): FocusSessionEntity?

    @Query("SELECT * FROM focus_sessions WHERE status IN ('RUNNING', 'PAUSED') ORDER BY started_at_millis DESC LIMIT 1")
    fun observeActive(): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions WHERE status = 'COMPLETED' AND target_decision = 'PENDING' ORDER BY completed_at_millis DESC LIMIT 1")
    fun observePendingTarget(): Flow<FocusSessionEntity?>

    @Query("UPDATE focus_sessions SET target_decision = :decision WHERE id = :id AND status = 'COMPLETED' AND target_decision = 'PENDING'")
    suspend fun resolveTarget(id: String, decision: String): Int

    @Query("SELECT DISTINCT task_id FROM focus_sessions WHERE status = 'COMPLETED' AND task_id IS NOT NULL")
    fun observeCompletedTaskIds(): Flow<List<String>>

    @Upsert
    suspend fun upsert(session: FocusSessionEntity)
}
