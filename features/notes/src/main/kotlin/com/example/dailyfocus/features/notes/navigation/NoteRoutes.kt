package com.example.dailyfocus.features.notes.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
object NoteListRoute : NavKey

/** A null ID creates a note; an existing ID opens it for editing. */
@Serializable
data class NoteEditorRoute(val noteId: String? = null) : NavKey
