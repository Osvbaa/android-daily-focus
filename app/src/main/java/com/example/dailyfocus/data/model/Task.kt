package com.example.dailyfocus.data.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import java.util.UUID

@Immutable
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)