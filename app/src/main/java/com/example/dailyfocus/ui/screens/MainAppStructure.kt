package com.example.dailyfocus.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppStructure() {
    var currentScreen by remember { mutableStateOf(value = "Tasks") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "Tasks",
                    onClick = { currentScreen = "Tasks" },
                    icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = "Tasks")},
                    label = { Text(text = "Tasks") },
                )

                NavigationBarItem(
                    selected = currentScreen == "Dashboard",
                    onClick = { currentScreen = "Dashboard" },
                    icon = { Icon(imageVector = Icons.Default.Info, contentDescription = "Estadísticas") },
                    label = { Text("Statistics") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "Tasks" -> TaskScreen()
                "Dashboard" -> DashboardMainScreen()
            }
        }
    }
}
