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
fun DashboardMainScreen(
    stats: List<StatItem>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(count = 2),
        modifier = modifier
            .padding(all = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)

    ) {
        items(
            items = stats,
            key = { item -> item.id },
            contentType = { "stat_type" }
        ) { item ->
            Dashboard(stats = item)
        }
    }
}




