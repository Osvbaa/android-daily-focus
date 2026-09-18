package com.example.dailyfocus.features.focustimer.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.data.repository.FocusSessionRepository
import com.example.dailyfocus.core.data.repository.FocusTargetDecisionRepository
import com.example.dailyfocus.core.model.FocusSessionStatus
import com.example.dailyfocus.core.model.FocusTimerPhase
import com.example.dailyfocus.core.model.HabitCompletionLevel
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.features.focustimer.ui.state.FocusTimerEvent
import com.example.dailyfocus.features.focustimer.ui.state.FocusTimerUiState
import com.example.dailyfocus.features.focustimer.service.FocusTimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class FocusTimerViewModel @Inject constructor(
    private val sessionRepository: FocusSessionRepository,
    private val targetDecisions: FocusTargetDecisionRepository,
    private val wallClock: WallClock,
    private val ids: IdGenerator,
    private val savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    private val requestedTaskId = savedStateHandle.get<String>(TASK_ID)?.takeIf(String::isNotBlank)?.let(::TaskId)
    private val requestedDurationSeconds = savedStateHandle.get<Long>(DURATION_SECONDS)?.takeIf { it in MIN_DURATION_SECONDS..MAX_DURATION_SECONDS }
    private val mutableState = MutableStateFlow(
        FocusTimerUiState(
            timer = com.example.dailyfocus.core.model.FocusTimerState(
                durationSeconds = requestedDurationSeconds ?: DEFAULT_DURATION_SECONDS,
                remainingSeconds = requestedDurationSeconds ?: DEFAULT_DURATION_SECONDS,
                linkedTaskId = requestedTaskId,
            ),
        ),
    )
    val uiState: StateFlow<FocusTimerUiState> = mutableState.asStateFlow()
    private var tickerJob: Job? = null
    private var completionHandled = false
    private val completionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            completeFromService(intent?.getStringExtra(FocusTimerService.EXTRA_SESSION_ID))
        }
    }

    init {
        ContextCompat.registerReceiver(
            context,
            completionReceiver,
            IntentFilter(FocusTimerService.ACTION_TIMER_FINISHED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        viewModelScope.launch {
            sessionRepository.observeActive().collect { session ->
                if (session == null || uiState.value.sessionId != null) return@collect
                val phase = if (session.status == FocusSessionStatus.PAUSED) FocusTimerPhase.PAUSED else FocusTimerPhase.RUNNING
                mutableState.value = FocusTimerUiState(
                    timer = com.example.dailyfocus.core.model.FocusTimerState(
                        phase = phase,
                        durationSeconds = session.durationSeconds,
                        remainingSeconds = session.remainingSeconds,
                        linkedTaskId = session.taskId,
                        linkedHabitId = session.habitId,
                        linkedHabitLevel = session.habitLevel,
                    ),
                    sessionId = session.id,
                )
                if (phase == FocusTimerPhase.RUNNING) startTickerIfNeeded()
            }
        }
        viewModelScope.launch {
            targetDecisions.observePending().collect { pending ->
                mutableState.update { it.copy(pendingTarget = pending) }
            }
        }
    }

    fun onEvent(event: FocusTimerEvent) {
        when (event) {
            FocusTimerEvent.Start -> start()
            FocusTimerEvent.Pause -> pause()
            FocusTimerEvent.Reset -> reset()
            FocusTimerEvent.ConfirmTarget -> resolveTarget(confirm = true)
            FocusTimerEvent.DismissTarget -> resolveTarget(confirm = false)
            is FocusTimerEvent.LinkTask -> linkTask(event.taskId)
            is FocusTimerEvent.LinkHabit -> linkHabit(event.habitId, event.level)
        }
    }

    private fun resolveTarget(confirm: Boolean) {
        val id = uiState.value.pendingTarget?.id ?: return
        viewModelScope.launch {
            val result = if (confirm) targetDecisions.confirm(id) else targetDecisions.dismiss(id)
            result.onFailure { error -> mutableState.update { it.copy(message = error.message ?: "No se pudo resolver el objetivo") } }
        }
    }

    private fun start() {
        val state = uiState.value
        if (state.timer.phase == FocusTimerPhase.IDLE) {
            val sessionId = ids.nextId()
            val started = wallClock.currentTimeMillis()
            completionHandled = false
            mutableState.update { it.copy(timer = it.timer.start(), sessionId = sessionId, message = null) }
            viewModelScope.launch {
                sessionRepository.start(
                    id = sessionId,
                    durationSeconds = uiState.value.timer.durationSeconds,
                    startedAtMillis = started,
                    taskId = uiState.value.timer.linkedTaskId,
                    habitId = uiState.value.timer.linkedHabitId,
                    habitLevel = uiState.value.timer.linkedHabitLevel,
                ).onSuccess {
                    startService(FocusTimerService.ACTION_START, uiState.value.timer.durationSeconds, sessionId)
                }.onFailure { error ->
                    mutableState.update { it.copy(timer = it.timer.reset(), sessionId = null, message = error.message) }
                }
            }
        } else if (state.timer.phase == FocusTimerPhase.PAUSED) {
            mutableState.update { it.copy(timer = it.timer.start(), message = null) }
            startService(FocusTimerService.ACTION_RESUME)
        }
        startTickerIfNeeded()
    }

    private fun pause() {
        if (uiState.value.timer.phase != FocusTimerPhase.RUNNING) return
        mutableState.update { it.copy(timer = it.timer.pause()) }
        startService(FocusTimerService.ACTION_PAUSE)
    }

    private fun reset() {
        tickerJob?.cancel()
        tickerJob = null
        startService(FocusTimerService.ACTION_FINISH)
        mutableState.update { it.copy(timer = it.timer.reset(), sessionId = null, message = null) }
        completionHandled = false
    }

    private fun linkTask(taskId: String?) {
        if (uiState.value.timer.phase == FocusTimerPhase.RUNNING) return
        mutableState.update {
            it.copy(timer = it.timer.copy(linkedTaskId = taskId?.takeIf(String::isNotBlank)?.let(::TaskId), linkedHabitId = null, linkedHabitLevel = null))
        }
    }

    private fun linkHabit(habitId: String?, level: HabitCompletionLevel) {
        if (uiState.value.timer.phase == FocusTimerPhase.RUNNING) return
        mutableState.update {
            it.copy(timer = it.timer.copy(linkedTaskId = null, linkedHabitId = habitId?.takeIf(String::isNotBlank)?.let(::HabitId), linkedHabitLevel = level))
        }
    }

    private fun startTickerIfNeeded() {
        if (uiState.value.timer.phase != FocusTimerPhase.RUNNING || tickerJob?.isActive == true) return
        tickerJob = viewModelScope.launch {
            while (isActive && uiState.value.timer.phase == FocusTimerPhase.RUNNING) {
                delay(1_000)
                val next = uiState.value.timer.tick()
                mutableState.update { it.copy(timer = next) }
                if (next.phase == FocusTimerPhase.COMPLETED && !completionHandled) {
                    break
                }
            }
        }
    }

    private fun completeFromService(sessionId: String?) {
        if (sessionId != null && sessionId != uiState.value.sessionId) return
        if (uiState.value.timer.phase == FocusTimerPhase.COMPLETED || completionHandled) return
        completionHandled = true
        tickerJob?.cancel()
        mutableState.update { it.copy(timer = it.timer.copy(phase = FocusTimerPhase.COMPLETED, remainingSeconds = 0L, completedSessions = it.timer.completedSessions + 1)) }
    }

    private fun startService(action: String, durationSeconds: Long? = null, sessionId: String? = null) {
        val intent = Intent(context, FocusTimerService::class.java).setAction(action)
        if (durationSeconds != null) intent.putExtra(FocusTimerService.EXTRA_DURATION_SECONDS, durationSeconds)
        if (sessionId != null) intent.putExtra(FocusTimerService.EXTRA_SESSION_ID, sessionId)
        runCatching { ContextCompat.startForegroundService(context, intent) }
    }

    override fun onCleared() {
        runCatching { context.unregisterReceiver(completionReceiver) }
        super.onCleared()
    }

    private companion object {
        const val TASK_ID = "taskId"
        const val DURATION_SECONDS = "durationSeconds"
        const val DEFAULT_DURATION_SECONDS = 25 * 60L
        const val MIN_DURATION_SECONDS = 60L
        const val MAX_DURATION_SECONDS = 8 * 60 * 60L
    }
}
