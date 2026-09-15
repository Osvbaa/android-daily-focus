package com.example.dailyfocus.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.dailyfocus.core.designsystem.theme.DailyFocusTheme
import com.example.dailyfocus.core.data.repository.TaskRepository
import com.example.dailyfocus.features.tasks.notification.TaskNotificationReceiver
import com.example.dailyfocus.features.tasks.reminder.TaskReminderScheduler
import com.example.dailyfocus.features.tasks.undo.TaskUndoRecoveryScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var taskRepository: TaskRepository
    private var notificationTaskId by mutableStateOf<String?>(null)

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationTaskId = notificationTaskIdFrom(intent)
        enableEdgeToEdge()
        TaskUndoRecoveryScheduler(this).schedule()
        lifecycleScope.launch {
            TaskReminderScheduler(this@MainActivity).sync(taskRepository.getReminderSettings())
        }
        setContent {
            DailyFocusTheme {
                MainAppStructure(initialTaskId = notificationTaskId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        notificationTaskId = notificationTaskIdFrom(intent)
    }

    private fun notificationTaskIdFrom(intent: Intent): String? =
        intent.takeIf { it.action == TaskNotificationReceiver.ACTION_OPEN }
            ?.getStringExtra(TaskNotificationReceiver.EXTRA_TASK_ID)
            ?.takeIf { it.isNotBlank() && it.length <= MAX_TASK_ID_LENGTH }

    private companion object { const val MAX_TASK_ID_LENGTH = 256 }
}
