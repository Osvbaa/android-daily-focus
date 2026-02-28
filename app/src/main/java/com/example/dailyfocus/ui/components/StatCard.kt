package com.example.dailyfocus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.dailyfocus.data.model.StatItem

@Composable
fun Dashboard(
    stats: StatItem,
    modifier: Modifier = Modifier
) {

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.LightGray)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Title",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    modifier = Modifier
                        .padding(all = 4.dp)
                )

                Text(
                    text = stats.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold,
                    modifier = Modifier
                        .padding(all = 4.dp)
                )

                Text(
                    text = stats.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    Dashboard(
        stats = StatItem(
            title = "Tasks",
            value = "10"
        )
    )
}