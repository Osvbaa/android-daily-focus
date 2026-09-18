package com.example.dailyfocus.features.projects.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailyfocus.core.data.repository.ProjectDraft
import com.example.dailyfocus.core.data.repository.ProjectMutationResult
import com.example.dailyfocus.core.data.repository.ProjectRepository
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.features.projects.ui.state.ProjectListEvent
import com.example.dailyfocus.features.projects.ui.state.ProjectListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val repository: ProjectRepository,
) : ViewModel() {
    private val selectedId = MutableStateFlow<ProjectId?>(null)
    private val localState = MutableStateFlow(ProjectListUiState())

    val uiState: StateFlow<ProjectListUiState> = combine(
        repository.observeProjects(),
        selectedId.flatMapLatest { id -> id?.let(repository::observeProject) ?: flowOf(null) },
        localState,
    ) { projects, selected, local ->
        local.copy(
            isLoading = false,
            projects = projects.toPersistentList(),
            selectedProject = selected,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectListUiState())

    fun onEvent(event: ProjectListEvent) {
        when (event) {
            is ProjectListEvent.NameChanged -> localState.update { it.copy(name = event.value, errorMessage = null) }
            is ProjectListEvent.DescriptionChanged -> localState.update { it.copy(description = event.value) }
            is ProjectListEvent.MilestonesChanged -> localState.update { it.copy(milestonesText = event.value) }
            ProjectListEvent.Create -> create()
            is ProjectListEvent.Select -> selectedId.value = ProjectId(event.id)
            is ProjectListEvent.Archive -> archive(ProjectId(event.id))
            ProjectListEvent.ClearSelection -> selectedId.value = null
        }
    }

    private fun create() {
        val current = localState.value
        viewModelScope.launch {
            when (val result = repository.createProject(ProjectDraft(
                name = current.name,
                description = current.description,
                milestones = current.milestonesText.lines().map(String::trim).filter(String::isNotBlank),
            ))) {
                is ProjectMutationResult.Success -> localState.update { it.copy(name = "", description = "", milestonesText = "", errorMessage = null) }
                is ProjectMutationResult.Failure -> localState.update { it.copy(errorMessage = result.cause.message ?: "No se pudo crear el proyecto") }
            }
        }
    }

    private fun archive(id: ProjectId) {
        viewModelScope.launch {
            when (val result = repository.archiveProject(id)) {
                is ProjectMutationResult.Success -> if (selectedId.value == id) selectedId.value = null
                is ProjectMutationResult.Failure -> localState.update { it.copy(errorMessage = result.cause.message ?: "No se pudo archivar") }
            }
        }
    }
}
