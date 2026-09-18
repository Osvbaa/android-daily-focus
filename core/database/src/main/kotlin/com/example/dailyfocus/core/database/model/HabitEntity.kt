package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val frequency: String,
    @ColumnInfo(name = "interval_days") val intervalDays: Int,
    @ColumnInfo(name = "occurrences_per_week") val occurrencesPerWeek: Int,
    @ColumnInfo(name = "target_level") val targetLevel: String,
    @ColumnInfo(name = "created_epoch_day") val createdEpochDay: Long,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
)

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habit_id", "epoch_day"],
    foreignKeys = [ForeignKey(
        entity = HabitEntity::class,
        parentColumns = ["id"],
        childColumns = ["habit_id"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index(value = ["habit_id"]), Index(value = ["epoch_day"])],
)
data class HabitCompletionEntity(
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val level: String,
)
