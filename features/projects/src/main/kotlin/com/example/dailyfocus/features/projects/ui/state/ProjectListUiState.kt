package com.example.dailyfocus.features.projects.ui.state

import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectDetails
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProjectListUiState(
    val isLoading: Boolean = true,
    val projects: ImmutableList<Project> = persistentListOf(),
    val selectedProject: ProjectDetails? = null,
    val name: String = "",
    val description: String = "",
    val milestonesText: String = "",
    val errorMessage: String? = null,
)

sealed interface ProjectListEvent {
    data class NameChanged(val value: String) : ProjectListEvent
    data class DescriptionChanged(val value: String) : ProjectListEvent
    data class MilestonesChanged(val value: String) : ProjectListEvent
    data object Create : ProjectListEvent
    data class Select(val id: String) : ProjectListEvent
    data class Archive(val id: String) : ProjectListEvent
    data object ClearSelection : ProjectListEvent
}
