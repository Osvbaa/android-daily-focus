package com.example.dailyfocus.ui.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.dailyfocus.data.model.Task

sealed class TaskState {
    object Loading : TaskState()
    object Empty : TaskState()

    data class Success(val tasks: List<Task>) : TaskState()
    data class Error(val message: String) : TaskState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun State() {
    var uiState by remember { mutableStateOf<TaskState>(value = TaskState.Loading) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Daily Focus")
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (val state = uiState) {
                is TaskState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is TaskState.Empty -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "No hay tareas"
                    )
                }

                is TaskState.Success -> {

                }

                is TaskState.Error -> {
                    Text(text = "Error: ${state.message}", color = Color.Red)
                }
            }
        }
    }
}
