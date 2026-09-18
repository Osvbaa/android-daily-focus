package com.example.dailyfocus.features.tasks.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyfocus.core.model.Task
import com.example.dailyfocus.core.model.Priority
import com.example.dailyfocus.core.ui.component.priorityColor

@Composable
fun TaskCard(
    task: Task,
    projectName: String? = null,
    requiresSubtaskReview: Boolean = false,
    onCheckedChange: () -> Unit,
    onTaskClick: () -> Unit,
    onMoreClick: () -> Unit,
    onFocusClick: (() -> Unit)? = null,
    onSourceNoteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCompleted = task.isCompleted

    val containerColor by animateColorAsState(
        targetValue = if (requiresSubtaskReview) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else if (task.isCompleted) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 300),
        label = "containerColor"
    )

    val textDecoration = if (isCompleted) TextDecoration.LineThrough else null
    val contentAlpha = if (isCompleted) 0.6f else 1f

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onTaskClick, onLongClick = onMoreClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor
        ),
        border = CardDefaults.outlinedCardBorder(enabled = task.isCompleted.not())
    ) {
        Row(
            modifier = Modifier
                .padding(all = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.width(width = 16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(contentAlpha),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = textDecoration
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                PriorityChip(task.priority, onTaskClick)

                val description = task.description
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (task.subtasks.isNotEmpty()) {
                    val completed = task.subtasks.count { it.isCompleted }
                    LinearProgressIndicator(
                        progress = { completed.toFloat() / task.subtasks.size },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text("$completed/${task.subtasks.size} subtareas", style = MaterialTheme.typography.labelSmall)
                }

                if (requiresSubtaskReview) {
                    Text(
                        text = "Focus terminado: revisa las subtareas pendientes",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                }

                projectName?.let {
                    Text(
                        text = "Proyecto: $it",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = task.dueDateEpochDays?.let { "Día $it" } ?: "Sin fecha",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            onFocusClick?.let { onClick ->
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.AccessTime, contentDescription = "Iniciar Focus")
                }
            }
            IconButton(onClick = onMoreClick) {
                Icon(Icons.Default.MoreVert, contentDescription = "Más acciones")
            }
            if (task.linkedNoteId != null && onSourceNoteClick != null) {
                IconButton(onClick = onSourceNoteClick) {
                    Icon(Icons.Default.Description, contentDescription = "Abrir nota de origen")
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(priority: Priority, onClick: () -> Unit) {
    if (priority == Priority.NONE) return
    val color = priorityColor(priority)
    AssistChip(
        onClick = onClick,
        label = { Text(priority.name.lowercase().replaceFirstChar { it.uppercase() }) },
        colors = AssistChipDefaults.assistChipColors(labelColor = color),
    )
}
