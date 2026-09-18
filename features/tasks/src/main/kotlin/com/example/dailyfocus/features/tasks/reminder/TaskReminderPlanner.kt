package com.example.dailyfocus.features.tasks.reminder

import com.example.dailyfocus.core.model.TaskReminderSettings
import java.time.Duration
import java.time.ZonedDateTime

object TaskReminderPlanner {
    fun shouldPublish(settings: TaskReminderSettings, todayEpochDay: Long, hasPendingTasks: Boolean): Boolean =
        settings.enabled && hasPendingTasks && settings.lastPublishedEpochDay != todayEpochDay

    fun delayUntilNextRun(settings: TaskReminderSettings, now: ZonedDateTime): Duration {
        val next = now.withHour(settings.hour).withMinute(settings.minute).withSecond(0).withNano(0)
            .let { candidate -> if (candidate.isAfter(now)) candidate else candidate.plusDays(1) }
        return Duration.between(now, next).coerceAtLeast(Duration.ofMinutes(1))
    }
}
