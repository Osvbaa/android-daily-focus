package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.core.model.ActivityEvent
import com.example.dailyfocus.core.model.ActivitySummary
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.StreakEventType
import com.example.dailyfocus.core.model.calculateActivitySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeActivityRepository : ActivityRepository {
    private val events = MutableStateFlow<List<ActivityEvent>>(emptyList())

    override fun observeEvents(): Flow<List<ActivityEvent>> = events

    override fun observeSummary(todayEpochDay: Long): Flow<ActivitySummary> =
        events.map { calculateActivitySummary(it, todayEpochDay) }

    override suspend fun recordCompletion(
        type: StreakEventType,
        sourceId: String,
        epochDay: Long,
        habitLevel: HabitCompletionLevel?,
    ): Result<Unit> {
        val key = "${type.name}:$sourceId:$epochDay"
        if (events.value.none { it.idempotencyKey == key }) {
            events.value += ActivityEvent(key, type, sourceId, epochDay, 0, 0L)
        }
        return Result.success(Unit)
    }
}
