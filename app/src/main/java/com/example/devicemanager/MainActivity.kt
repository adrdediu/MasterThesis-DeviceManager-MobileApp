package com.example.devicemanager

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.devicemanager.ui.theme.DeviceManagerTheme
import com.example.devicemanager.ui.screens.ConnectionScreen
import com.example.devicemanager.ui.theme.BootstrapPrimary
import com.example.devicemanager.viewmodels.ConnectionViewModel
import kotlinx.coroutines.delay

@Composable
fun MainContent() {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("device_manager", Context.MODE_PRIVATE)
    val savedUrl = remember { sharedPrefs.getString("saved_url", null) }
    val (currentUrl, setCurrentUrl) = remember { mutableStateOf<String?>(savedUrl) }
    val (isLoading, setIsLoading) = remember { mutableStateOf(true) }
    val viewModel: ConnectionViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val activity = (context as? ComponentActivity)
    LaunchedEffect(Unit) {
        delay(1500) // Show loading for 1.5 seconds
        if (savedUrl != null) {
            val finalUrl = when {
                savedUrl.startsWith("http://") || savedUrl.startsWith("https://") -> savedUrl
                else -> "https://$savedUrl"
            }

            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(android.graphics.Color.TRANSPARENT)
                .build()

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, ResetUrlReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setDefaultColorSchemeParams(colorSchemeParams)
                .setCloseButtonPosition(CustomTabsIntent.CLOSE_BUTTON_POSITION_START)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setToolbarColor(android.graphics.Color.TRANSPARENT)
                .addMenuItem("Reset Connection", pendingIntent)
                .setDownloadButtonEnabled(false)
                .build()

            customTabsIntent.launchUrl(context, Uri.parse(finalUrl))
            activity?.finish()


        }
        setIsLoading(false)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingScreen()
            savedUrl == null -> ConnectionScreen(
                onNavigateToDeviceScreen = {
                    sharedPrefs.edit().putString("saved_url", uiState.deviceId).apply()
                    setCurrentUrl(uiState.deviceId)
                }
            )
        }
    }
}


@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = BootstrapPrimary,
            modifier = Modifier.size(48.dp)
        )
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DeviceManagerTheme {
                MainContent()
            }
        }
    }
}
