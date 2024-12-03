package com.example.devicemanager.viewmodels

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.webkit.URLUtil
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.example.devicemanager.ResetUrlReceiver
import com.example.devicemanager.ui.screens.ConnectionScreenState
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Loading : ConnectionState()
    data class Connected(val serverIp: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}


class ConnectionViewModel : ViewModel() {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _uiState = MutableStateFlow(ConnectionScreenState())
    val uiState: StateFlow<ConnectionScreenState> = _uiState.asStateFlow()

    fun updateDeviceId(deviceId: String) {
        _uiState.update { it.copy(deviceId = deviceId) }
    }

    fun connect(context: Context, onNavigateToDeviceScreen: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val sharedPrefs = context.getSharedPreferences("device_manager", Context.MODE_PRIVATE)
            val address = uiState.value.deviceId
            if (isValidAddress(address)) {


                val finalUrl = when {
                    address.startsWith("http://") || address.startsWith("https://") -> address
                    else -> "https://192.168.0.100"
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

                Toast.makeText(context, "Server found! Connecting...", Toast.LENGTH_SHORT).show()
                delay(1000)


                sharedPrefs.edit().putString("saved_url", finalUrl).apply()
                customTabsIntent.launchUrl(context, Uri.parse(finalUrl))

            } else {
                Toast.makeText(context, "Failed to find server", Toast.LENGTH_SHORT).show()
                _uiState.update { it.copy(error = "Invalid server address !") }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }




    private fun isValidAddress(address: String): Boolean {
        val ipPattern = """^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".toRegex()
        return address.matches(ipPattern) || URLUtil.isValidUrl("https://$address")
    }

}