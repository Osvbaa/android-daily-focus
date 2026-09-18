package com.example.dailyfocus.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.dailyfocus.core.database.model.TaskDraftEntity
import com.example.dailyfocus.core.database.model.TaskDraftSubtaskEntity
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.model.Subtask
import com.example.dailyfocus.core.model.SubtaskId
import com.example.dailyfocus.core.model.TaskDraft
import com.example.dailyfocus.core.model.TaskDraftRecord
import com.example.dailyfocus.core.model.TaskId
import com.example.dailyfocus.core.model.ProjectId
import com.example.dailyfocus.core.model.MilestoneId
import kotlinx.collections.immutable.toPersistentList

@Dao
interface TaskDraftDao {
    @Query("SELECT * FROM task_drafts WHERE `key` = :key")
    suspend fun getDraft(key: String): TaskDraftEntity?

    @Query("SELECT * FROM task_draft_subtasks WHERE draft_key = :key ORDER BY position ASC")
    suspend fun getSubtasks(key: String): List<TaskDraftSubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDraft(entity: TaskDraftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubtasks(entities: List<TaskDraftSubtaskEntity>)

    @Query("DELETE FROM task_draft_subtasks WHERE draft_key = :key")
    suspend fun deleteSubtasks(key: String)

    @Query("DELETE FROM task_drafts WHERE `key` = :key")
    suspend fun deleteDraft(key: String): Int

    @Transaction
    suspend fun getRecord(key: String): TaskDraftRecord? {
        val entity = getDraft(key) ?: return null
        val subtasks = getSubtasks(key).map { row ->
            Subtask(
                id = SubtaskId(row.id),
                taskId = TaskId(entity.taskId ?: "draft"),
                title = row.title,
                isCompleted = row.isCompleted,
                position = row.position,
            )
        }.toPersistentList()
        return TaskDraftRecord(
            key = entity.key,
            taskId = entity.taskId?.let(::TaskId),
            draft = TaskDraft(
                title = entity.title,
                description = entity.description,
                dueDateEpochDays = entity.dueDateEpochDays,
                priority = runCatching { Priority.valueOf(entity.priority) }.getOrDefault(Priority.NONE),
                estimatedDurationSeconds = entity.estimatedDurationSeconds,
                projectId = entity.projectId?.let(::ProjectId),
                milestoneId = entity.milestoneId?.let(::MilestoneId),
                subtasks = subtasks,
            ),
            baseRevision = entity.baseRevision,
            draftRevision = entity.draftRevision,
            updatedAtMillis = entity.updatedAtMillis,
        )
    }

    @Transaction
    suspend fun saveRecord(record: TaskDraftRecord) {
        upsertDraft(
            TaskDraftEntity(
                key = record.key,
                taskId = record.taskId?.value,
                title = record.draft.title,
                description = record.draft.description,
                dueDateEpochDays = record.draft.dueDateEpochDays,
                priority = record.draft.priority.name,
                estimatedDurationSeconds = record.draft.estimatedDurationSeconds,
                baseRevision = record.baseRevision,
                draftRevision = record.draftRevision,
                updatedAtMillis = record.updatedAtMillis,
                projectId = record.draft.projectId?.value,
                milestoneId = record.draft.milestoneId?.value,
            ),
        )
        deleteSubtasks(record.key)
        if (record.draft.subtasks.isNotEmpty()) {
            upsertSubtasks(record.draft.subtasks.mapIndexed { index, subtask ->
                TaskDraftSubtaskEntity(
                    draftKey = record.key,
                    id = subtask.id.value,
                    title = subtask.title,
                    isCompleted = subtask.isCompleted,
                    position = index,
                )
            })
        }
    }
}
