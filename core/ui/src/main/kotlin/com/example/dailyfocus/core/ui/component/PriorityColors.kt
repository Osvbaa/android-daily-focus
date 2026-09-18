package com.example.dailyfocus.core.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.dailyfocus.core.model.Priority

@Composable
fun priorityColor(priority: Priority): Color = when (priority) {
    Priority.URGENT -> MaterialTheme.colorScheme.error
    Priority.HIGH -> MaterialTheme.colorScheme.tertiary
    Priority.NORMAL -> MaterialTheme.colorScheme.primary
    Priority.LOW -> MaterialTheme.colorScheme.secondary
    Priority.NONE -> MaterialTheme.colorScheme.onSurfaceVariant
}
