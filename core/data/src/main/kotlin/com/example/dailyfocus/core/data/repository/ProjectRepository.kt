package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.model.MilestoneId
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectDetails
import com.example.dailyfocus.core.model.ProjectMilestone
import com.example.dailyfocus.core.model.ProjectId
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

sealed interface ProjectMutationResult {
    data class Success(val projectId: ProjectId) : ProjectMutationResult
    data class Failure(val cause: Throwable) : ProjectMutationResult
}

data class ProjectDraft(
    val name: String,
    val description: String = "",
    val milestones: List<String> = emptyList(),
)

interface ProjectRepository {
    fun observeProjects(): Flow<List<Project>>
    fun observeProject(id: ProjectId): Flow<ProjectDetails?>
    suspend fun createProject(draft: ProjectDraft): ProjectMutationResult
    suspend fun renameProject(id: ProjectId, name: String): ProjectMutationResult
    suspend fun archiveProject(id: ProjectId): ProjectMutationResult
    suspend fun activeProjects(): List<Project>
    suspend fun addMilestone(projectId: ProjectId, title: String, position: Int): Result<MilestoneId>

    /** Completes actionable milestones whose assigned tasks are all complete and activates the next one. */
    suspend fun reconcileProgress(projectId: ProjectId): Result<List<MilestoneId>> = Result.success(emptyList())
}
