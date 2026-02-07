package com.example.dailyfocus.data.model

import java.time.LocalDateTime

data class Task(val isCompleted: Boolean, val title: String, val description: String?, val date: LocalDateTime)