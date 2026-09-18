package com.example.dailyfocus.core.model

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable

@JvmInline @Serializable value class TaskId(val value: String) { override fun toString() = value }
@JvmInline @Serializable value class SubtaskId(val value: String) { override fun toString() = value }

@Serializable
data class Subtask(
    val id: SubtaskId,
    val taskId: TaskId,
    val title: String,
    val isCompleted: Boolean = false,
    val position: Int,
)

@Serializable
data class TaskDraft(
    val title: String = "",
    val description: String = "",
    val dueDateEpochDays: Long? = null,
    val priority: Priority = Priority.NONE,
    /** Optional focus estimate; sessions propose 25 minutes when it is absent. */
    val estimatedDurationSeconds: Long? = null,
    /** A task can be assigned to at most one project. */
    val projectId: ProjectId? = null,
    val milestoneId: MilestoneId? = null,
    val subtasks: ImmutableList<Subtask> = persistentListOf(),
)

/** A draft persisted independently from a published task aggregate. */
@Serializable
data class TaskDraftRecord(
    val key: String,
    val taskId: TaskId? = null,
    val draft: TaskDraft = TaskDraft(),
    val baseRevision: Long? = null,
    val draftRevision: Long = 0L,
    val updatedAtMillis: Long = 0L,
)

/** Durable completion operation used to implement an individual Undo action. */
@Serializable
data class TaskUndoOperation(
    val operationId: String,
    val taskId: TaskId,
    val taskWasCompleted: Boolean,
    val subtaskCompletion: ImmutableList<Pair<SubtaskId, Boolean>> = persistentListOf(),
    val createdElapsedMillis: Long,
    val expiresAtElapsedMillis: Long,
    /** Revision of the aggregate after completion; protects Undo from later edits. */
    val expectedRevision: Long,
)

enum class ReschedulePreset { TODAY, TOMORROW, THIS_WEEKEND, NEXT_WEEK }

data class TaskCompletionSnapshot(
    val taskId: TaskId,
    val taskWasCompleted: Boolean,
    val subtaskCompletion: ImmutableList<Pair<SubtaskId, Boolean>>,
    val operationId: String? = null,
    val createdElapsedMillis: Long = 0L,
    val expiresAtElapsedMillis: Long = Long.MAX_VALUE,
    val expectedRevision: Long = 0L,
    val previousRevision: Long = 0L,
)

data class Task(
    val id: TaskId,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val priority: Priority = Priority.NONE,
    /** Optional focus estimate selected while planning this task. */
    val estimatedDurationSeconds: Long? = null,
    val linkedNoteId: String? = null,
    /** Null means the task is independent; otherwise it belongs to exactly one project. */
    val projectId: ProjectId? = null,
    val milestoneId: MilestoneId? = null,
    val createdAtMillis: Long,
    val dueDateEpochDays: Long? = null,
    val subtasks: ImmutableList<Subtask> = persistentListOf(),
    /** Monotonically increasing aggregate revision used for conflict detection. */
    val revision: Long = 0L,
) {
    val createdAt: LocalDateTime
        get() = Instant.ofEpochMilli(createdAtMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
}
