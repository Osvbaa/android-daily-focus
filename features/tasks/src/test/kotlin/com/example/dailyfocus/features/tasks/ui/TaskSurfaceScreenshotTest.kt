package com.example.dailyfocus.features.tasks.ui

import com.example.dailyfocus.core.designsystem.theme.DailyFocusTheme
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.features.tasks.ui.screen.TaskEditorScreen
import com.example.dailyfocus.features.tasks.ui.screen.TaskListScreen
import com.example.dailyfocus.features.tasks.ui.state.TaskEditorUiState
import com.example.dailyfocus.features.tasks.ui.state.TaskListUiState
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.github.takahirom.roborazzi.fontScale
import com.github.takahirom.roborazzi.locale
import com.github.takahirom.roborazzi.size
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.LooperMode
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w720dp-h900dp-xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TaskSurfaceScreenshotTest {
    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun taskListSurface() {
        captureRoboImage(
            filePath = "src/test/screenshots/TaskSurfaceScreenshotTest_taskListSurface.png",
            roborazziComposeOptions = RoborazziComposeOptions {
                size(widthDp = 720, heightDp = 900)
                locale("es")
                fontScale(1f)
            },
        ) {
            DailyFocusTheme(dynamicColor = false) {
                TaskListScreen(
                    state = TaskListUiState(isLoading = false, tasks = persistentListOf(sampleTask())),
                    onEvent = {},
                )
            }
        }
    }

    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun editorSurface() {
        captureRoboImage(
            filePath = "src/test/screenshots/TaskSurfaceScreenshotTest_editorSurface.png",
            roborazziComposeOptions = RoborazziComposeOptions {
                size(widthDp = 720, heightDp = 900)
                locale("es")
                fontScale(1f)
            },
        ) {
            DailyFocusTheme(dynamicColor = false) {
                TaskEditorScreen(
                    state = TaskEditorUiState(taskId = TaskId("task"), draft = sampleTask().toDraft()),
                    onEvent = {},
                )
            }
        }
    }

    private fun sampleTask() = Task(
        id = TaskId("task"),
        title = "Preparar revisión semanal",
        description = "Concentrar los tres resultados más importantes.",
        createdAtMillis = 0,
        dueDateEpochDays = 20_700,
        subtasks = persistentListOf(
            Subtask(SubtaskId("one"), TaskId("task"), "Revisar métricas", isCompleted = true, position = 0),
            Subtask(SubtaskId("two"), TaskId("task"), "Compartir resumen", position = 1),
        ),
    )
}

private fun Task.toDraft() = com.example.dailyfocus.core.model.TaskDraft(
    title = title,
    description = description.orEmpty(),
    dueDateEpochDays = dueDateEpochDays,
    priority = priority,
    subtasks = subtasks,
)
