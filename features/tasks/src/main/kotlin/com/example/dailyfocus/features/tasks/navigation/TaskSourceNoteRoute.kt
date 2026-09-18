package com.example.dailyfocus.features.tasks.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Read-only origin note destination. Only the primitive note ID crosses navigation. */
@Serializable
data class TaskSourceNoteRoute(val noteId: String) : NavKey
