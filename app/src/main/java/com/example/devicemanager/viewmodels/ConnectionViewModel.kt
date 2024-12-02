package com.example.devicemanager.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.webkit.URLUtil

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Loading : ConnectionState()
    data class Connected(val serverIp: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

class ConnectionViewModel : ViewModel() {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    fun connectToServer(address: String) {
        viewModelScope.launch {
            _connectionState.value = ConnectionState.Loading
            try {
                delay(1500)
                if (isValidAddress(address)) {
                    _connectionState.value = ConnectionState.Connected(address)
                } else {
                    _connectionState.value = ConnectionState.Error("Invalid address format")
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            }
        }
    }

    private fun isValidAddress(address: String): Boolean {
        val ipPattern = """^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$""".toRegex()
        return address.matches(ipPattern) || URLUtil.isValidUrl("https://$address")
    }
}