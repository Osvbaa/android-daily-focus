package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class ProjectId(val value: String)

@Serializable
data class Project(val id: ProjectId, val name: String, val description: String = "", val isArchived: Boolean = false, val createdAtMillis: Long)

@JvmInline
@Serializable
value class MilestoneId(val value: String) { override fun toString() = value }

@Serializable
enum class MilestoneStatus { LOCKED, ACTIVE, COMPLETED }

@Serializable
data class ProjectMilestone(
    val id: MilestoneId,
    val projectId: ProjectId,
    val title: String,
    val position: Int,
    val status: MilestoneStatus = if (position == 0) MilestoneStatus.ACTIVE else MilestoneStatus.LOCKED,
)

data class ProjectDetails(
    val project: Project,
    val milestones: kotlinx.collections.immutable.ImmutableList<ProjectMilestone> =
        kotlinx.collections.immutable.persistentListOf(),
    val tasks: kotlinx.collections.immutable.ImmutableList<Task> =
        kotlinx.collections.immutable.persistentListOf(),
)
