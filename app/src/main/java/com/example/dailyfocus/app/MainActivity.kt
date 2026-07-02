package com.example.dailyfocus.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.example.dailyfocus.app.ui.MainAppStructure
import com.example.dailyfocus.core.designsystem.theme.DailyFocusTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyFocusTheme {
                MainAppStructure()
            }
        }
    }
}