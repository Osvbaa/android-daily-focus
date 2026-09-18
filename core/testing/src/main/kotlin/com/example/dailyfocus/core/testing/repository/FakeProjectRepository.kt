package com.example.dailyfocus.core.testing.repository

import com.example.dailyfocus.core.data.repository.ProjectDraft
import com.example.dailyfocus.core.data.repository.ProjectMutationResult
import com.example.dailyfocus.core.data.repository.ProjectRepository
import com.example.dailyfocus.core.model.MilestoneId
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectDetails
import com.example.dailyfocus.core.model.ProjectId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeProjectRepository : ProjectRepository {
    private val projects = MutableStateFlow<List<Project>>(emptyList())
    private val details = MutableStateFlow<Map<ProjectId, ProjectDetails>>(emptyMap())

    fun seed(vararg values: Project) {
        projects.value = values.toList()
        details.value = values.associate { it.id to ProjectDetails(it) }
    }

    override fun observeProjects(): Flow<List<Project>> = projects

    override fun observeProject(id: ProjectId): Flow<ProjectDetails?> = details.map { it[id] }

    override suspend fun createProject(draft: ProjectDraft): ProjectMutationResult =
        ProjectMutationResult.Failure(UnsupportedOperationException("Not needed by this fake"))

    override suspend fun renameProject(id: ProjectId, name: String): ProjectMutationResult =
        ProjectMutationResult.Failure(UnsupportedOperationException("Not needed by this fake"))

    override suspend fun archiveProject(id: ProjectId): ProjectMutationResult =
        ProjectMutationResult.Failure(UnsupportedOperationException("Not needed by this fake"))

    override suspend fun activeProjects(): List<Project> = projects.value.filterNot { it.isArchived }

    override suspend fun addMilestone(projectId: ProjectId, title: String, position: Int): Result<MilestoneId> =
        Result.failure(UnsupportedOperationException("Not needed by this fake"))
}
