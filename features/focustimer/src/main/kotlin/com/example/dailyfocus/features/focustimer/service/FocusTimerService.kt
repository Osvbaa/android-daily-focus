package com.example.dailyfocus.features.focustimer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.dailyfocus.core.common.time.DateProvider
import com.example.dailyfocus.core.data.repository.ActivityRepository
import com.example.dailyfocus.core.data.repository.FocusSessionRepository
import com.example.dailyfocus.core.common.time.WallClock
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first

/** Keeps the timer alive while the app is backgrounded; elapsed time is monotonic. */
@AndroidEntryPoint
class FocusTimerService : Service() {
    @Inject lateinit var sessionRepository: FocusSessionRepository
    @Inject lateinit var activityRepository: ActivityRepository
    @Inject lateinit var dates: DateProvider
    @Inject lateinit var wallClock: WallClock
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var deadlineElapsed: Long = 0L
    private var remainingWhenPaused: Long = 0L
    private var running = false
    private var sessionId: String? = null
    private var sessionFinished = false
    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            val remaining = ((deadlineElapsed - SystemClock.elapsedRealtime()) / 1_000L).coerceAtLeast(0L)
            updateNotification(remaining)
            if (remaining == 0L) {
                finishSession()
            } else {
                handler.postDelayed(this, 1_000L)
            }
        }
    }
    private val handler = android.os.Handler(mainLooper)

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            null -> restoreActiveSession()
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION_SECONDS, 0L)
                if (duration > 0L) {
                    sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                    sessionFinished = false
                    remainingWhenPaused = duration
                    deadlineElapsed = SystemClock.elapsedRealtime() + duration * 1_000L
                    running = true
                }
            }
            ACTION_PAUSE -> {
                if (running) remainingWhenPaused = ((deadlineElapsed - SystemClock.elapsedRealtime()) / 1_000L).coerceAtLeast(0L)
                running = false
                persistStatus(com.example.dailyfocus.core.model.FocusSessionStatus.PAUSED, remainingWhenPaused)
            }
            ACTION_RESUME -> {
                deadlineElapsed = SystemClock.elapsedRealtime() + remainingWhenPaused * 1_000L
                running = remainingWhenPaused > 0L
                persistStatus(
                    status = com.example.dailyfocus.core.model.FocusSessionStatus.RUNNING,
                    remaining = remainingWhenPaused,
                    deadlineAtMillis = wallClock.currentTimeMillis() + remainingWhenPaused * 1_000L,
                )
            }
            ACTION_FINISH -> {
                cancelSession()
            }
            ACTION_ADD_FIVE -> {
                val remaining = if (running) ((deadlineElapsed - SystemClock.elapsedRealtime()) / 1_000L).coerceAtLeast(0L) else remainingWhenPaused
                remainingWhenPaused = remaining + 5 * 60L
                if (running) {
                    deadlineElapsed = SystemClock.elapsedRealtime() + remainingWhenPaused * 1_000L
                    persistStatus(
                        status = com.example.dailyfocus.core.model.FocusSessionStatus.RUNNING,
                        remaining = remainingWhenPaused,
                        deadlineAtMillis = wallClock.currentTimeMillis() + remainingWhenPaused * 1_000L,
                    )
                } else {
                    persistStatus(com.example.dailyfocus.core.model.FocusSessionStatus.PAUSED, remainingWhenPaused)
                }
            }
        }
        startForeground(NOTIFICATION_ID, notification(if (running) ((deadlineElapsed - SystemClock.elapsedRealtime()) / 1_000L) else remainingWhenPaused))
        handler.removeCallbacks(tick)
        if (running) handler.post(tick)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun updateNotification(remaining: Long) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(remaining))
    }

    private fun notification(remaining: Long): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_media_play)
        .setContentTitle("Focus Timer")
        .setContentText("${remaining / 60}:${(remaining % 60).toString().padStart(2, '0')}")
        .setOngoing(running)
        .addAction(action(if (running) "Pausar" else "Reanudar", if (running) ACTION_PAUSE else ACTION_RESUME))
        .addAction(action("Finalizar", ACTION_FINISH))
        .addAction(action("+5 min", ACTION_ADD_FIVE))
        .build()

    private fun action(label: String, action: String): NotificationCompat.Action =
        NotificationCompat.Action.Builder(
            android.R.drawable.ic_media_pause,
            label,
            PendingIntent.getService(
                this,
                action.hashCode(),
                Intent(this, FocusTimerService::class.java).setAction(action),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            ),
        ).build()

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Focus Timer", NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun finishSession() {
        if (sessionFinished) return
        sessionFinished = true
        running = false
        val id = sessionId
        if (id == null) {
            sendBroadcast(Intent(ACTION_TIMER_FINISHED).setPackage(packageName))
            stopSelf()
            return
        }
        serviceScope.launch {
            val session = sessionRepository.get(id)
            if (session != null && session.status != com.example.dailyfocus.core.model.FocusSessionStatus.COMPLETED) {
                sessionRepository.updateStatus(
                    id,
                    com.example.dailyfocus.core.model.FocusSessionStatus.COMPLETED,
                    completedAtMillis = wallClock.currentTimeMillis(),
                    remainingSeconds = 0L,
                )
                activityRepository.recordPomodoro(id, dates.today().toEpochDay())
            }
            sendBroadcast(Intent(ACTION_TIMER_FINISHED).setPackage(packageName).putExtra(EXTRA_SESSION_ID, id))
            stopSelf()
        }
    }

    private fun cancelSession() {
        if (sessionFinished) return
        sessionFinished = true
        running = false
        handler.removeCallbacks(tick)
        val id = sessionId
        if (id == null) {
            stopSelf()
            return
        }
        serviceScope.launch {
            sessionRepository.updateStatus(
                id,
                com.example.dailyfocus.core.model.FocusSessionStatus.CANCELED,
                remainingSeconds = remainingWhenPaused,
            )
            stopSelf()
        }
    }

    /** Rebuilds the in-memory monotonic deadline after Android recreates a sticky service. */
    private fun restoreActiveSession() {
        serviceScope.launch {
            val session = sessionRepository.observeActive().first() ?: run {
                stopSelf()
                return@launch
            }
            sessionId = session.id
            sessionFinished = false
            if (session.status == com.example.dailyfocus.core.model.FocusSessionStatus.PAUSED) {
                running = false
                remainingWhenPaused = session.remainingSeconds
            } else {
                val now = wallClock.currentTimeMillis()
                remainingWhenPaused = session.deadlineAtMillis
                    ?.let { deadline -> ((deadline - now + 999L) / 1_000L).coerceAtLeast(0L) }
                    ?: session.remainingSeconds
                deadlineElapsed = SystemClock.elapsedRealtime() + remainingWhenPaused * 1_000L
                running = remainingWhenPaused > 0L
                if (session.deadlineAtMillis == null) {
                    persistStatus(
                        status = com.example.dailyfocus.core.model.FocusSessionStatus.RUNNING,
                        remaining = remainingWhenPaused,
                        deadlineAtMillis = now + remainingWhenPaused * 1_000L,
                    )
                }
            }
            handler.post {
                startForeground(NOTIFICATION_ID, notification(remainingWhenPaused))
                handler.removeCallbacks(tick)
                if (running) handler.post(tick) else if (remainingWhenPaused == 0L) finishSession()
            }
        }
    }

    private fun persistStatus(
        status: com.example.dailyfocus.core.model.FocusSessionStatus,
        remaining: Long,
        deadlineAtMillis: Long? = null,
    ) {
        val id = sessionId ?: return
        serviceScope.launch {
            sessionRepository.updateStatus(
                id = id,
                status = status,
                remainingSeconds = remaining,
                deadlineAtMillis = deadlineAtMillis,
            )
        }
    }

    companion object {
        const val ACTION_START = "com.example.dailyfocus.focustimer.START"
        const val ACTION_PAUSE = "com.example.dailyfocus.focustimer.PAUSE"
        const val ACTION_RESUME = "com.example.dailyfocus.focustimer.RESUME"
        const val ACTION_FINISH = "com.example.dailyfocus.focustimer.FINISH"
        const val ACTION_ADD_FIVE = "com.example.dailyfocus.focustimer.ADD_FIVE"
        const val ACTION_TIMER_FINISHED = "com.example.dailyfocus.focustimer.FINISHED"
        const val EXTRA_DURATION_SECONDS = "duration_seconds"
        const val EXTRA_SESSION_ID = "session_id"
        private const val CHANNEL_ID = "focus_timer"
        private const val NOTIFICATION_ID = 4201
    }
}
