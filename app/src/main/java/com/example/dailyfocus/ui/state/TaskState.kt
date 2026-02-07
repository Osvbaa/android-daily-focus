package com.example.dailyfocus.ui.state

sealed class TaskState {
    object Loading : TaskState()
    object Success : TaskState()
    object Empty : TaskState()
}