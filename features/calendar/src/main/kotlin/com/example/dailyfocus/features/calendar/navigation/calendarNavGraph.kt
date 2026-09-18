package com.example.dailyfocus.features.calendar.navigation

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import com.example.dailyfocus.features.calendar.ui.screen.CalendarScreen
import com.example.dailyfocus.features.calendar.ui.viewmodel.CalendarViewModel

fun calendarNavGraph(key: NavKey): NavEntry<NavKey>? = when (key) {
    CalendarRoute -> NavEntry(key) {
        val viewModel: CalendarViewModel = hiltViewModel()
        val state by viewModel.uiState.collectAsStateWithLifecycle()
        CalendarScreen(state)
    }
    else -> null
}
