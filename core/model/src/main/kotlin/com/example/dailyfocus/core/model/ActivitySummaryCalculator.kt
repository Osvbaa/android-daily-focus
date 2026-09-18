package com.example.dailyfocus.core.model

/** Pure streak calculation: multiple events on the same day count as one day. */
fun calculateActivitySummary(events: List<ActivityEvent>, todayEpochDay: Long): ActivitySummary {
    val days = events.map { it.epochDay }.toSet()
    val current = if (todayEpochDay in days) {
        generateSequence(todayEpochDay) { previous ->
            (previous - 1).takeIf { it in days }
        }.count()
    } else {
        0
    }
    val longest = days.sorted().fold(0 to 0) { (best, run), day ->
        val nextRun = if (day - 1 in days) run + 1 else 1
        maxOf(best, nextRun) to nextRun
    }.first
    return ActivitySummary(
        currentStreakDays = current,
        longestStreakDays = longest,
        totalPoints = events.sumOf { it.points },
        todayPoints = events.filter { it.epochDay == todayEpochDay }.sumOf { it.points },
    )
}
