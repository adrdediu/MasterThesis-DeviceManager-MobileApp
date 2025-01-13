package com.example.devicemanager

import android.app.Activity
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
    val (isLoading, setIsLoading) = remember { mutableStateOf(true) }
    val viewModel: ConnectionViewModel = viewModel()

    LaunchedEffect(Unit) {
        delay(500)
        if (savedUrl != null) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra("url", savedUrl)
            }
            context.startActivity(intent)
            (context as? Activity)?.finish()
        }
        setIsLoading(false)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> LoadingScreen()
            savedUrl == null -> ConnectionScreen(
                onNavigateToDeviceScreen = {
                    val newUrl = viewModel.uiState.value.deviceId
                    sharedPrefs.edit().putString("saved_url", newUrl).apply()
                    val intent = Intent(context, WebViewActivity::class.java).apply {
                        putExtra("url", newUrl)
                    }
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
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
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionManager = PermissionManager(this)
        permissionManager.checkAndRequestPermissions()

        setContent {
            DeviceManagerTheme {
                val permissionsGranted by permissionManager.permissionsGranted.collectAsState()

                if (permissionsGranted) {
                    MainContent()
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}
