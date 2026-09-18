package com.example.dailyfocus.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val updatedAtMillis: Long,
    val createdAtMillis: Long
)