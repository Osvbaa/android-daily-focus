package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.dispatchers.AppDispatchers
import com.example.dailyfocus.core.common.dispatchers.Dispatcher
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.dao.ActivityDao
import com.example.dailyfocus.core.database.model.asEntity
import com.example.dailyfocus.core.database.model.asExternalModel
import com.example.dailyfocus.core.model.ActivityEvent
import com.example.dailyfocus.core.model.ActivitySummary
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.StreakEventType
import com.example.dailyfocus.core.model.calculateActivitySummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class OfflineFirstActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val wallClock: WallClock,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ActivityRepository {
    override fun observeEvents(): Flow<List<ActivityEvent>> = activityDao.observeAll()
        .map { rows -> rows.map { it.asExternalModel() } }
        .flowOn(ioDispatcher)

    override fun observeSummary(todayEpochDay: Long): Flow<ActivitySummary> = observeEvents()
        .map { events -> calculateActivitySummary(events, todayEpochDay) }

    override suspend fun recordCompletion(
        type: StreakEventType,
        sourceId: String,
        epochDay: Long,
        habitLevel: HabitCompletionLevel?,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            require(sourceId.isNotBlank()) { "Activity source ID must not be blank" }
            val category = type.category()
            val key = "$category:$sourceId:$epochDay"
            val existing = activityDao.get(key)
            if (existing != null && type != StreakEventType.HABIT_LEVEL) return@withContext Result.success(Unit)

            val desiredPoints = when (type) {
                StreakEventType.TASK, StreakEventType.PROJECT_TASK ->
                    if (activityDao.countCategoryOnDay(CATEGORY_TASK, epochDay) == 0) TASK_POINTS else 0
                StreakEventType.PROJECT_MILESTONE ->
                    if (activityDao.countCategoryOnDay(CATEGORY_MILESTONE, epochDay) == 0) MILESTONE_POINTS else 0
                StreakEventType.HABIT_LEVEL -> habitLevel.points()
                StreakEventType.POMODORO ->
                    if (activityDao.countCategoryOnDay(CATEGORY_FOCUS, epochDay) < FOCUS_DAILY_LIMIT) FOCUS_POINTS else 0
            }
            val oldPoints = existing?.points ?: 0
            val available = (GLOBAL_DAILY_LIMIT - activityDao.pointsOnDay(epochDay) + oldPoints).coerceAtLeast(0)
            val points = if (type == StreakEventType.HABIT_LEVEL) {
                maxOf(oldPoints, minOf(desiredPoints, oldPoints + available))
            } else {
                minOf(desiredPoints, available)
            }
            val event = ActivityEvent(
                idempotencyKey = key,
                type = type,
                sourceId = sourceId,
                epochDay = epochDay,
                points = points,
                occurredAtMillis = wallClock.currentTimeMillis(),
            )
            activityDao.upsert(event.asEntity(category))
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private fun StreakEventType.category(): String = when (this) {
        StreakEventType.TASK, StreakEventType.PROJECT_TASK -> CATEGORY_TASK
        StreakEventType.PROJECT_MILESTONE -> CATEGORY_MILESTONE
        StreakEventType.HABIT_LEVEL -> CATEGORY_HABIT
        StreakEventType.POMODORO -> CATEGORY_FOCUS
    }

    private fun HabitCompletionLevel?.points(): Int = when (this) {
        HabitCompletionLevel.MINI -> 3
        HabitCompletionLevel.PLUS -> 5
        HabitCompletionLevel.ELITE -> 8
        null -> 0
    }

    private companion object {
        const val CATEGORY_TASK = "TASK"
        const val CATEGORY_MILESTONE = "MILESTONE"
        const val CATEGORY_HABIT = "HABIT"
        const val CATEGORY_FOCUS = "FOCUS"
        const val TASK_POINTS = 5
        const val MILESTONE_POINTS = 10
        const val FOCUS_POINTS = 1
        const val FOCUS_DAILY_LIMIT = 4
        const val GLOBAL_DAILY_LIMIT = 25
    }
}
