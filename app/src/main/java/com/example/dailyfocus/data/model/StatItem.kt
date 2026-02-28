package com.example.dailyfocus.data.model

import java.util.UUID

data class StatItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val value: String
)