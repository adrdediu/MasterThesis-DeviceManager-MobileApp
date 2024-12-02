package com.example.devicemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.devicemanager.ui.screens.ConnectionScreen
import com.example.devicemanager.ui.theme.DeviceManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeviceManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ConnectionScreen()
                }
            }
        }
    }
}
