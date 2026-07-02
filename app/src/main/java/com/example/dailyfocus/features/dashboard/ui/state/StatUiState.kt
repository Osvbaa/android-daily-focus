package com.example.dailyfocus.features.dashboard.ui.state

import java.util.UUID

// data class con las propiedades de una estadistica
data class StatUiState(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val value: Int
)