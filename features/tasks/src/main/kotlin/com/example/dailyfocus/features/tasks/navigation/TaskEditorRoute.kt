package com.example.dailyfocus.features.tasks.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/** A null task ID opens creation; an existing ID opens editing. */
@Serializable
@SerialName("com.example.dailyfocus.features.tasks.navigation.TaskDetailRoute")
data class TaskEditorRoute(val taskId: String? = null) : NavKey
