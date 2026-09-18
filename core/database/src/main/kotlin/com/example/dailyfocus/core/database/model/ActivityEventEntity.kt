package com.example.dailyfocus.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_events",
    indices = [Index(value = ["epoch_day"]), Index(value = ["category", "epoch_day"])],
)
data class ActivityEventEntity(
    @PrimaryKey @ColumnInfo(name = "idempotency_key") val idempotencyKey: String,
    val type: String,
    @ColumnInfo(name = "source_id") val sourceId: String?,
    @ColumnInfo(name = "epoch_day") val epochDay: Long,
    val category: String,
    val points: Int,
    @ColumnInfo(name = "occurred_at_millis") val occurredAtMillis: Long,
    @ColumnInfo(name = "rules_version") val rulesVersion: Int,
)
