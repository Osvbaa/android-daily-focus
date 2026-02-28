package com.example.dailyfocus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.data.model.StatItem
import com.example.dailyfocus.ui.components.Dashboard

@Composable
fun DashboardMainScreen(modifier: Modifier = Modifier) {
    val dashboardItems = remember {
        mutableStateListOf(
            StatItem(title = "Tasks", value = "15"),
            StatItem(title = "Completed", value = "5"),
            StatItem(title = "To Do", value = "5"),
            StatItem(title = "In Progress", value = "2"),
            StatItem(title = "Overdue", value = "1"),
            StatItem(title = "Upcoming", value = "1")
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 2),
        modifier = modifier
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)

    ) {
        items(
            items = dashboardItems,
            key = { item -> item.id },
            contentType = { "stat_type" }
        ) { item ->
            Dashboard(stats = item)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    DashboardMainScreen()
}



