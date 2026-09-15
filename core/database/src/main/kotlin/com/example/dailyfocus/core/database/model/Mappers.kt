package com.example.dailyfocus.core.database.model

import com.example.dailyfocus.core.model.Note
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.Priority
import kotlinx.collections.immutable.toPersistentList

fun TaskEntity.asExternalModel(linkedNoteId: String? = null) = Task(
    id = TaskId(id),
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDateEpochDays = dueDateEpochDays,
    priority = priority,
    linkedNoteId = linkedNoteId,
    createdAtMillis = createdAtMillis,
    revision = revision,
)

fun Task.asEntity() = TaskEntity(
    id = id.value,
    title = title,
    description = description,
    isCompleted = isCompleted,
    dueDateEpochDays = dueDateEpochDays,
    priority = priority,
    createdAtMillis = createdAtMillis,
    revision = revision,
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
        linkedNoteId = linkedNoteId ?: first.linkedNoteId,
        createdAtMillis = first.taskCreatedAtMillis,
        revision = first.taskRevision,
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
