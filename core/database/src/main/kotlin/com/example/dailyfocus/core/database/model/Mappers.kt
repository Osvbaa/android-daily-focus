package com.example.dailyfocus.core.database.model

import com.example.dailyfocus.core.model.Note
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Project
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.core.model.ProjectMilestone
import com.example.dailyfocus.core.model.MilestoneId
import com.example.dailyfocus.core.model.MilestoneStatus
import com.example.dailyfocus.core.model.Habit
import com.example.dailyfocus.core.model.HabitId
import com.example.dailyfocus.core.model.HabitFrequency
import com.example.dailyfocus.core.model.HabitCompletion
import com.example.dailyfocus.core.model.HabitCompletionLevel
import kotlinx.collections.immutable.toPersistentList

fun TaskEntity.asExternalModel(linkedNoteId: String? = null) = Task(
    id = TaskId(id),
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDateEpochDays = dueDateEpochDays,
    priority = priority,
    estimatedDurationSeconds = estimatedDurationSeconds,
    linkedNoteId = linkedNoteId,
    createdAtMillis = createdAtMillis,
    revision = revision,
    projectId = projectId?.let(::ProjectId),
    milestoneId = milestoneId?.let(::MilestoneId),
)

fun Task.asEntity() = TaskEntity(
    id = id.value,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDateEpochDays = dueDateEpochDays,
    priority = priority,
    estimatedDurationSeconds = estimatedDurationSeconds,
    createdAtMillis = createdAtMillis,
    revision = revision,
    projectId = projectId?.value,
    milestoneId = milestoneId?.value,
)

fun NoteEntity.asExternalModel() = Note(
    id = id,
    title = title,
    content = content,
    updatedAtMillis = updatedAtMillis,
    createdAtMillis = createdAtMillis
)

fun Note.asEntity() = NoteEntity(
    id = id,
    title = title,
    content = content,
    updatedAtMillis = updatedAtMillis,
    createdAtMillis = createdAtMillis
)

fun SubtaskEntity.asExternalModel() = Subtask(
    id = SubtaskId(id), taskId = TaskId(taskId), title = title,
    isCompleted = isCompleted, position = position,
)

fun Subtask.asEntity() = SubtaskEntity(
    id = id.value, taskId = taskId.value, title = title,
    isCompleted = isCompleted, position = position,
)

fun List<TaskRow>.asExternalModels(linkedNoteId: String? = null) =
    groupBy { it.taskId }.values.map { rows -> rows.asExternalModel(linkedNoteId) }

fun List<TaskRow>.asExternalModelOrNull(linkedNoteId: String? = null): Task? =
    if (isEmpty()) null else asExternalModel(linkedNoteId)

fun List<TaskRow>.asExternalModel(linkedNoteId: String? = null): Task {
    val first = requireNotNull(firstOrNull())
    return Task(
        id = TaskId(first.taskId),
        title = first.taskTitle,
        description = first.taskDescription,
        isCompleted = first.taskIsCompleted,
        dueDateEpochDays = first.taskDueDateEpochDays,
        priority = Priority.valueOf(first.taskPriority),
        estimatedDurationSeconds = first.taskEstimatedDurationSeconds,
        linkedNoteId = linkedNoteId ?: first.linkedNoteId,
        createdAtMillis = first.taskCreatedAtMillis,
        revision = first.taskRevision,
        projectId = first.projectId?.let(::ProjectId),
        milestoneId = first.milestoneId?.let(::MilestoneId),
        subtasks = filter { it.subtaskId != null }
            .sortedBy { it.subtaskPosition }
            .map { row ->
                Subtask(
                    id = SubtaskId(requireNotNull(row.subtaskId)),
                    taskId = TaskId(first.taskId),
                    title = requireNotNull(row.subtaskTitle),
                    isCompleted = requireNotNull(row.subtaskIsCompleted),
                    position = requireNotNull(row.subtaskPosition),
                )
            }.toPersistentList(),
    )
}

fun ProjectEntity.asExternalModel() = Project(
    id = ProjectId(id), name = name, description = description,
    isArchived = isArchived, createdAtMillis = createdAtMillis,
)

fun Project.asEntity() = ProjectEntity(
    id = id.value, name = name, description = description,
    isArchived = isArchived, createdAtMillis = createdAtMillis,
)

fun ProjectMilestoneEntity.asExternalModel() = ProjectMilestone(
    id = MilestoneId(id), projectId = ProjectId(projectId), title = title,
    position = position, status = runCatching { MilestoneStatus.valueOf(status) }
        .getOrDefault(MilestoneStatus.LOCKED),
)

fun ProjectMilestone.asEntity() = ProjectMilestoneEntity(
    id = id.value, projectId = projectId.value, title = title,
    position = position, status = status.name,
)

fun HabitEntity.asExternalModel() = Habit(
    id = HabitId(id), name = name,
    frequency = runCatching { HabitFrequency.valueOf(frequency) }.getOrDefault(HabitFrequency.DAILY),
    intervalDays = intervalDays, occurrencesPerWeek = occurrencesPerWeek,
    targetLevel = runCatching { HabitCompletionLevel.valueOf(targetLevel) }.getOrDefault(HabitCompletionLevel.ELITE),
    createdEpochDay = createdEpochDay, isArchived = isArchived,
)

fun Habit.asEntity() = HabitEntity(
    id = id.value, name = name, frequency = frequency.name,
    intervalDays = intervalDays, occurrencesPerWeek = occurrencesPerWeek,
    targetLevel = targetLevel.name, createdEpochDay = createdEpochDay,
    isArchived = isArchived,
)

fun HabitCompletionEntity.asExternalModel() = HabitCompletion(
    habitId = HabitId(habitId), epochDay = epochDay,
    level = runCatching { HabitCompletionLevel.valueOf(level) }.getOrDefault(HabitCompletionLevel.MINI),
)

fun HabitCompletion.asEntity() = HabitCompletionEntity(
    habitId = habitId.value, epochDay = epochDay, level = level.name,
)
