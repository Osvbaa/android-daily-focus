package com.example.dailyfocus.features.focustimer.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class FocusTimerRoute(
    val taskId: String? = null,
    val durationSeconds: Long? = null,
) : NavKey
