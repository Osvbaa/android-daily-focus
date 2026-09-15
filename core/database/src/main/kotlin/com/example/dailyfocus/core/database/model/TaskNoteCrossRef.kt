package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "task_note_cross_ref",
    primaryKeys = ["task_id", "note_id"],
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["note_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["task_id"]),
        Index(value = ["note_id"])
    ]
)
data class TaskNoteCrossRef(
    @ColumnInfo(name = "task_id")
    val taskId: String,
    @ColumnInfo(name = "note_id")
    val noteId: String
)