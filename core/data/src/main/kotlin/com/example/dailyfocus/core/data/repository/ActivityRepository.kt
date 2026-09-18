package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.ActivityEvent
import com.example.dailyfocus.core.model.ActivitySummary
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.StreakEventType
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun observeEvents(): Flow<List<ActivityEvent>>
    fun observeSummary(todayEpochDay: Long): Flow<ActivitySummary>

    /** Records an idempotent completion and applies the local v1 points policy. */
    suspend fun recordCompletion(
        type: StreakEventType,
        sourceId: String,
        epochDay: Long,
        habitLevel: HabitCompletionLevel? = null,
    ): Result<Unit>

    /** A pomodoro source ID must identify the persisted focus session. */
    suspend fun recordPomodoro(sessionId: String, epochDay: Long): Result<Unit> =
        recordCompletion(StreakEventType.POMODORO, sessionId, epochDay)
}
