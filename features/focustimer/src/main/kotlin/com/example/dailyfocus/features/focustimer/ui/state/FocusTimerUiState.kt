package com.example.dailyfocus.features.focustimer.ui.state

import com.example.dailyfocus.core.model.FocusTimerPhase
import com.example.dailyfocus.core.model.FocusTimerState
import com.example.dailyfocus.core.model.FocusSession

data class FocusTimerUiState(
    val timer: FocusTimerState = FocusTimerState(),
    val sessionId: String? = null,
    val message: String? = null,
    val pendingTarget: FocusSession? = null,
) {
    val phase: FocusTimerPhase get() = timer.phase
    val minutes: Long get() = timer.remainingSeconds / 60
    val seconds: Long get() = timer.remainingSeconds % 60
}

sealed interface FocusTimerEvent {
    data object Start : FocusTimerEvent
    data object Pause : FocusTimerEvent
    data object Reset : FocusTimerEvent
    data object ConfirmTarget : FocusTimerEvent
    data object DismissTarget : FocusTimerEvent
    data class LinkTask(val taskId: String?) : FocusTimerEvent
    data class LinkHabit(val habitId: String?, val level: com.example.dailyfocus.core.model.HabitCompletionLevel = com.example.dailyfocus.core.model.HabitCompletionLevel.ELITE) : FocusTimerEvent
}
