package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.dispatchers.AppDispatchers
import com.example.dailyfocus.core.common.dispatchers.Dispatcher
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.database.dao.HabitDao
import com.example.dailyfocus.core.database.model.asEntity
import com.example.dailyfocus.core.database.model.asExternalModel
import com.example.dailyfocus.core.model.HabitCompletion
import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.isDueOn
import com.example.dailyfocus.core.model.startOfCivilWeek
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstHabitRepository @Inject constructor(
    private val habitDao: HabitDao,
    private val idGenerator: IdGenerator,
    private val dates: DateProvider,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : HabitRepository {
    override fun observeHabits() = habitDao.observeHabits().map { rows -> rows.map { it.asExternalModel() } }.flowOn(ioDispatcher)

    override fun observeCompletions(habitId: HabitId): Flow<List<HabitCompletion>> =
        habitDao.observeCompletions(habitId.value).map { rows -> rows.map { it.asExternalModel() } }.flowOn(ioDispatcher)

    override suspend fun createHabit(draft: HabitDraft): Result<HabitId> = withContext(ioDispatcher) {
        runCatching {
            require(draft.name.isNotBlank()) { "Habit name must not be blank" }
            if (draft.frequency == HabitFrequency.EVERY_N_DAYS) {
                require(draft.intervalDays in 1..365) { "Interval must be between 1 and 365 days" }
            }
            if (draft.frequency == HabitFrequency.TIMES_PER_WEEK) {
                require(draft.occurrencesPerWeek in 1..7) { "Weekly occurrences must be between 1 and 7" }
            }
            val id = HabitId(idGenerator.nextId())
            habitDao.upsertHabit(
                Habit(
                    id = id, name = draft.name.trim(), frequency = draft.frequency,
                    intervalDays = draft.intervalDays, occurrencesPerWeek = draft.occurrencesPerWeek,
                    createdEpochDay = dates.today().toEpochDay(),
                ).asEntity(),
            )
            id
        }
    }

    override suspend fun completeLevel(habitId: HabitId, epochDay: Long, level: HabitCompletionLevel): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val habit = habitDao.getHabit(habitId.value) ?: error("Habit not found")
            check(!habit.isArchived) { "Habit is archived" }
            val model = habit.asExternalModel()
            check(model.isDueOn(epochDay)) { "Habit is not scheduled for this day" }
            val weekStart = epochDay.startOfCivilWeek()
            val weekCompletions = habitDao.getCompletions(weekStart, weekStart + 6)
                .filter { it.habitId == habitId.value }
            if (model.frequency == HabitFrequency.WEEKLY) {
                check(weekCompletions.all { it.epochDay == epochDay }) { "Weekly habit already completed" }
            }
            if (model.frequency == HabitFrequency.TIMES_PER_WEEK) {
                val distinctDays = weekCompletions.map { it.epochDay }.distinct()
                check(epochDay in distinctDays || distinctDays.size < model.occurrencesPerWeek) {
                    "Weekly occurrence target already reached"
                }
            }
            val existing = weekCompletions.firstOrNull { it.epochDay == epochDay }
            if (existing != null) {
                val current = HabitCompletionLevel.valueOf(existing.level)
                check(level.ordinal >= current.ordinal) { "Habit level cannot be reduced" }
            }
            habitDao.upsertCompletion(HabitCompletion(habitId, epochDay, level).asEntity())
            Unit
        }
    }

    override suspend fun archiveHabit(habitId: HabitId): Result<Unit> = withContext(ioDispatcher) {
        runCatching { check(habitDao.setArchived(habitId.value, true) == 1) { "Habit not found" } }
    }

}
