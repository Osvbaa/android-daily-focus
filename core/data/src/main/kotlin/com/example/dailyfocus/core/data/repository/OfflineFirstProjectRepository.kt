package com.example.dailyfocus.core.data.repository

import com.example.dailyfocus.core.common.dispatchers.AppDispatchers
import com.example.dailyfocus.core.common.dispatchers.Dispatcher
import com.example.dailyfocus.core.common.time.IdGenerator
import com.example.dailyfocus.core.common.time.WallClock
import com.example.dailyfocus.core.database.dao.ProjectDao
import com.example.dailyfocus.core.database.model.asEntity
import com.example.dailyfocus.core.database.model.asExternalModel
import com.example.dailyfocus.core.model.MilestoneId
import com.example.dailyfocus.core.model.MilestoneStatus
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectDetails
import com.example.dailyfocus.core.model.ProjectId
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineFirstProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val taskRepository: TaskRepository,
    private val idGenerator: IdGenerator,
    private val wallClock: WallClock,
    @Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ProjectRepository {
    override fun observeProjects() = projectDao.observeProjects()
        .map { it.map(com.example.dailyfocus.core.database.model.ProjectEntity::asExternalModel) }
        .flowOn(ioDispatcher)

    override fun observeProject(id: ProjectId): Flow<ProjectDetails?> = combine(
        projectDao.observeProject(id.value),
        projectDao.observeMilestones(id.value),
        taskRepository.observeTasks(),
    ) { project, milestones, tasks ->
        project?.let {
            ProjectDetails(
                project = it.asExternalModel(),
                milestones = milestones.map { milestone -> milestone.asExternalModel() }.toPersistentList(),
                tasks = tasks.filter { task -> task.projectId == id }.toPersistentList(),
            )
        }
    }.flowOn(ioDispatcher)

    override suspend fun createProject(draft: ProjectDraft): ProjectMutationResult = mutate {
        require(draft.name.isNotBlank()) { "Project name must not be blank" }
        val normalizedMilestones = draft.milestones.map(String::trim)
        require(normalizedMilestones.distinct().size == normalizedMilestones.size) { "Duplicate milestone titles" }
        val id = ProjectId(idGenerator.nextId())
        val milestones = normalizedMilestones.mapIndexed { position, title ->
            require(title.isNotBlank()) { "Milestone title must not be blank" }
            com.example.dailyfocus.core.model.ProjectMilestone(
                id = MilestoneId(idGenerator.nextId()), projectId = id,
                title = title.trim(), position = position,
                status = if (position == 0) MilestoneStatus.ACTIVE else MilestoneStatus.LOCKED,
            ).asEntity()
        }
        projectDao.createProjectWithMilestones(
            Project(id, draft.name.trim(), draft.description.trim(), createdAtMillis = wallClock.currentTimeMillis()).asEntity(),
            milestones,
        )
        ProjectMutationResult.Success(id)
    }

    override suspend fun renameProject(id: ProjectId, name: String): ProjectMutationResult = mutate {
        require(name.isNotBlank()) { "Project name must not be blank" }
        val current = projectDao.getActiveProjects().firstOrNull { it.id == id.value }
            ?: error("Project not found")
        projectDao.upsertProject(current.copy(name = name.trim()))
        ProjectMutationResult.Success(id)
    }

    override suspend fun archiveProject(id: ProjectId): ProjectMutationResult = mutate {
        check(projectDao.setArchived(id.value, true) == 1) { "Project not found" }
        ProjectMutationResult.Success(id)
    }

    override suspend fun activeProjects(): List<Project> = withContext(ioDispatcher) {
        projectDao.getActiveProjects().map { it.asExternalModel() }
    }

    override suspend fun addMilestone(projectId: ProjectId, title: String, position: Int): Result<MilestoneId> = withContext(ioDispatcher) {
        runCatching {
            require(title.isNotBlank()) { "Milestone title must not be blank" }
            check(projectDao.getActiveProjects().any { it.id == projectId.value }) { "Project not found" }
            val id = MilestoneId(idGenerator.nextId())
            projectDao.upsertMilestone(
                com.example.dailyfocus.core.model.ProjectMilestone(
                    id, projectId, title.trim(), position,
                    if (position == 0) MilestoneStatus.ACTIVE else MilestoneStatus.LOCKED,
                ).asEntity(),
            )
            id
        }
    }

    override suspend fun reconcileProgress(projectId: ProjectId): Result<List<MilestoneId>> = withContext(ioDispatcher) {
        runCatching {
            val project = checkNotNull(projectDao.getProject(projectId.value)) { "Project not found" }
            check(!project.isArchived) { "Project is archived" }
            val milestones = projectDao.getMilestones(projectId.value)
            val tasks = taskRepository.observeTasks().first().filter { it.projectId == projectId }
            val completed = mutableListOf<MilestoneId>()
            var active = milestones.firstOrNull { it.status == MilestoneStatus.ACTIVE.name }
            while (active != null) {
                val current = checkNotNull(active)
                val milestoneTasks = tasks.filter { it.milestoneId?.value == current.id }
                if (milestoneTasks.isEmpty() || milestoneTasks.any { !it.isCompleted }) {
                    active = null
                } else {
                    projectDao.setMilestoneStatus(current.id, MilestoneStatus.COMPLETED.name)
                    completed += MilestoneId(current.id)
                    val next = milestones.firstOrNull { it.position > current.position && it.status == MilestoneStatus.LOCKED.name }
                    next?.let {
                        projectDao.setMilestoneStatus(it.id, MilestoneStatus.ACTIVE.name)
                    }
                    active = next
                }
            }
            completed
        }
    }

    private suspend fun mutate(block: suspend () -> ProjectMutationResult): ProjectMutationResult = withContext(ioDispatcher) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            ProjectMutationResult.Failure(error)
        }
    }
}
